package com.mobileforge.util

import kotlinx.coroutines.*
import java.io.*
import com.mobileforge.ui.BuildResult

class ProcessExecutor {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun execute(command: String, workingDir: String, onOutput: (String) -> Unit) {
        scope.launch {
            try {
                val parts = command.split(" ")
                val processBuilder = ProcessBuilder(*parts.toTypedArray())
                processBuilder.directory(File(workingDir))
                processBuilder.redirectErrorStream(true)
                processBuilder.environment().put("ANDROID_HOME", "/opt/android-sdk")
                processBuilder.environment().put("PATH", "/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:" + System.getenv("PATH"))

                val process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    withContext(Dispatchers.Main) {
                        onOutput(line ?: "")
                    }
                }
                process.waitFor()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onOutput("Error: ${e.message}")
                }
            }
        }
    }

    fun buildAndroid(projectPath: String): BuildResult {
        return try {
            val projectDir = File(projectPath)
            if (!projectDir.exists()) {
                return BuildResult(false, null, listOf("Project directory does not exist: $projectPath"), emptyList())
            }

            // Check for gradlew
            val gradlew = File(projectDir, "gradlew")
            val gradleCmd = if (gradlew.exists()) "./gradlew" else "gradle"

            val buildProcess = if (gradlew.exists()) {
                gradlew.setExecutable(true)
                ProcessBuilder("./gradlew", "assembleDebug")
                    .directory(projectDir)
                    .redirectErrorStream(true)
                    .start()
            } else {
                ProcessBuilder("gradle", "assembleDebug")
                    .directory(projectDir)
                    .redirectErrorStream(true)
                    .start()
            }

            val output = StringBuilder()
            val reader = BufferedReader(InputStreamReader(buildProcess.inputStream))
            var line: String?
            val errors = mutableListOf<String>()

            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
                if (line?.contains("error:", ignoreCase = true) == true ||
                    line?.contains("FAILED", ignoreCase = true) == true) {
                    errors.add(line ?: "")
                }
            }

            val exitCode = buildProcess.waitFor()

            if (exitCode == 0) {
                val apkFile = File(projectDir, "app/build/outputs/apk/debug/app-debug.apk")
                if (apkFile.exists()) {
                    BuildResult(true, apkFile.absolutePath, emptyList(), emptyList())
                } else {
                    BuildResult(true, output.toString(), emptyList(), emptyList())
                }
            } else {
                BuildResult(false, null, errors, emptyList())
            }
        } catch (e: Exception) {
            BuildResult(false, null, listOf(e.message ?: "Build failed"), emptyList())
        }
    }
}