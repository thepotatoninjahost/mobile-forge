package com.mobileforge.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.io.*
import java.nio.file.*
import org.json.JSONArray
import org.json.JSONObject
import com.mobileforge.util.ProcessExecutor
import com.mobileforge.util.FileManager

data class Project(
    val name: String,
    val path: String,
    val type: String,
    val lastModified: Long
)

data class EditorTab(
    val filePath: String,
    val fileName: String,
    val content: String,
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorCol: Int = 1
)

@kotlinx.serialization.Serializable
data class BuildResult(
    val success: Boolean,
    val outputPath: String?,
    val errors: List<String>,
    val warnings: List<String>
)

class IDEViewModel : ViewModel() {
    private val processExecutor = ProcessExecutor()
    private val fileManager = FileManager()

    var currentProjectPath by mutableStateOf("/storage/emulated/0/MobileForge")
        private set

    var editorTabs by mutableStateOf<List<EditorTab>>(emptyList())
        private set

    var activeTabIndex by mutableStateOf(0)
        private set

    var files by mutableStateOf<List<FileItem>>(emptyList())
        private set

    var terminalOutput by mutableStateOf<List<String>>(emptyList())
        private set

    var buildInProgress by mutableStateOf(false)
        private set

    var buildResult by mutableStateOf<BuildResult?>(null)
        private set

    var isDarkTheme by mutableStateOf(true)
        private set

    var currentLanguage by mutableStateOf("kotlin")
        private set

    var consoleLogs by mutableStateOf<List<ConsoleEntry>>(emptyList())
        private set

    init {
        loadProject()
    }

    fun loadProject() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = File(currentProjectPath)
                if (!root.exists()) root.mkdirs()
                files = fileManager.loadDirectoryTree(currentProjectPath)
                appendTerminal("📁 Project loaded: $currentProjectPath")
            } catch (e: Exception) {
                appendTerminal("❌ Error loading project: ${e.message}")
            }
        }
    }

    fun openFile(file: FileItem) {
        if (file.isDirectory) {
            currentProjectPath = file.path
            loadProject()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = editorTabs.indexOfFirst { it.filePath == file.path }
                if (existing >= 0) {
                    activeTabIndex = existing
                } else {
                    val content = File(file.path).readText()
                    val lang = detectLanguage(file.name)
                    val tab = EditorTab(file.path, file.name, content)
                    editorTabs = editorTabs + tab
                    activeTabIndex = editorTabs.size - 1
                    currentLanguage = lang
                }
            } catch (e: Exception) {
                appendTerminal("❌ Error opening file: ${e.message}")
            }
        }
    }

    fun updateTabContent(content: String) {
        if (activeTabIndex < 0 || activeTabIndex >= editorTabs.size) return
        val tab = editorTabs[activeTabIndex]
        editorTabs = editorTabs.toMutableList().apply {
            this[activeTabIndex] = tab.copy(content = content, isModified = true)
        }
    }

    fun saveCurrentTab() {
        if (activeTabIndex < 0 || activeTabIndex >= editorTabs.size) return
        val tab = editorTabs[activeTabIndex]
        viewModelScope.launch(Dispatchers.IO) {
            try {
                File(tab.filePath).writeText(tab.content)
                editorTabs = editorTabs.toMutableList().apply {
                    this[activeTabIndex] = tab.copy(isModified = false)
                }
                appendTerminal("💾 Saved: ${tab.fileName}")
            } catch (e: Exception) {
                appendTerminal("❌ Save failed: ${e.message}")
            }
        }
    }

    fun closeTab(index: Int) {
        if (index < 0 || index >= editorTabs.size) return
        editorTabs = editorTabs.toMutableList().apply { removeAt(index) }
        if (activeTabIndex >= editorTabs.size) {
            activeTabIndex = (editorTabs.size - 1).coerceAtLeast(0)
        }
    }

    fun createFile(name: String, content: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val path = "$currentProjectPath/$name"
                File(path).apply {
                    parentFile?.mkdirs()
                    writeText(content)
                }
                appendTerminal("✨ Created: $name")
                loadProject()
            } catch (e: Exception) {
                appendTerminal("❌ Create failed: ${e.message}")
            }
        }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val path = "$currentProjectPath/$name"
                File(path).mkdirs()
                appendTerminal("📁 Created directory: $name")
                loadProject()
            } catch (e: Exception) {
                appendTerminal("❌ mkdir failed: ${e.message}")
            }
        }
    }

    fun deleteItem(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.isDirectory) file.deleteRecursively() else file.delete()
                editorTabs = editorTabs.filter { it.filePath != path }
                appendTerminal("🗑️ Deleted: ${file.name}")
                loadProject()
            } catch (e: Exception) {
                appendTerminal("❌ Delete failed: ${e.message}")
            }
        }
    }

    fun renameItem(oldPath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldFile = File(oldPath)
                val newPath = "${oldFile.parent}/$newName"
                oldFile.renameTo(File(newPath))
                editorTabs = editorTabs.map {
                    if (it.filePath == oldPath) it.copy(filePath = newPath, fileName = newName)
                    else it
                }
                appendTerminal("✏️ Renamed to: $newName")
                loadProject()
            } catch (e: Exception) {
                appendTerminal("❌ Rename failed: ${e.message}")
            }
        }
    }

    fun runCommand(command: String) {
        appendTerminal("\$ $command")
        viewModelScope.launch(Dispatchers.IO) {
            processExecutor.execute(command, currentProjectPath) { output ->
                appendTerminal(output)
            }
        }
    }

    fun buildProject() {
        buildInProgress = true
        buildResult = null
        appendTerminal("🔨 Starting build...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = processExecutor.buildAndroid(currentProjectPath)
                buildResult = result
                if (result.success) {
                    appendTerminal("✅ Build successful!")
                    appendTerminal("📦 APK: ${result.outputPath}")
                } else {
                    appendTerminal("❌ Build failed")
                    result.errors.forEach { appendTerminal("  ERROR: $it") }
                }
            } catch (e: Exception) {
                buildResult = BuildResult(false, null, listOf(e.message ?: "Unknown error"), emptyList())
                appendTerminal("❌ Build error: ${e.message}")
            } finally {
                buildInProgress = false
            }
        }
    }

    fun gitClone(repoUrl: String) {
        appendTerminal("📥 Cloning $repoUrl...")
        viewModelScope.launch(Dispatchers.IO) {
            processExecutor.execute("git clone $repoUrl \"$currentProjectPath\"", "/storage/emulated/0") { output ->
                appendTerminal(output)
            }
        }
    }

    fun gitPush(message: String) {
        appendTerminal("📤 Pushing to remote...")
        viewModelScope.launch(Dispatchers.IO) {
            processExecutor.execute("git add -A && git commit -m \"$message\" && git push", currentProjectPath) { output ->
                appendTerminal(output)
            }
        }
    }

    fun gitPull() {
        appendTerminal("📥 Pulling from remote...")
        viewModelScope.launch(Dispatchers.IO) {
            processExecutor.execute("git pull", currentProjectPath) { output ->
                appendTerminal(output)
            }
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    private fun appendTerminal(text: String) {
        terminalOutput = terminalOutput + text
    }

    private fun detectLanguage(fileName: String): String {
        return when {
            fileName.endsWith(".kt") -> "kotlin"
            fileName.endsWith(".java") -> "java"
            fileName.endsWith(".xml") -> "xml"
            fileName.endsWith(".gradle") -> "gradle"
            fileName.endsWith(".json") -> "json"
            fileName.endsWith(".js") -> "javascript"
            fileName.endsWith(".ts") -> "typescript"
            fileName.endsWith(".html") -> "html"
            fileName.endsWith(".css") -> "css"
            fileName.endsWith(".py") -> "python"
            fileName.endsWith(".rs") -> "rust"
            fileName.endsWith(".go") -> "go"
            fileName.endsWith(".c") -> "c"
            fileName.endsWith(".cpp") -> "cpp"
            fileName.endsWith(".h") -> "c"
            fileName.endsWith(".md") -> "markdown"
            fileName.endsWith(".sh") -> "shell"
            fileName.endsWith(".yaml") -> "yaml"
            fileName.endsWith(".yml") -> "yaml"
            fileName.endsWith(".toml") -> "toml"
            else -> "text"
        }
    }
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val children: List<FileItem> = emptyList()
)

data class ConsoleEntry(
    val text: String,
    val type: String // "info", "error", "success", "command"
)
