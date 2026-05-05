package com.mobileforge.util

import com.mobileforge.ui.FileItem
import java.io.File

class FileManager {

    fun loadDirectoryTree(path: String, depth: Int = 0): List<FileItem> {
        if (depth > 5) return emptyList() // Prevent infinite recursion

        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) return emptyList()

        return directory.listFiles()?.sortedWith(
            compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
        )?.mapNotNull { file ->
            try {
                if (file.name.startsWith(".")) return@mapNotNull null

                if (file.isDirectory) {
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = true,
                        size = 0,
                        children = loadDirectoryTree(file.absolutePath, depth + 1)
                    )
                } else {
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = false,
                        size = file.length()
                    )
                }
            } catch (e: SecurityException) {
                null
            }
        } ?: emptyList()
    }

    fun getFileContent(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            File(path).apply {
                parentFile?.mkdirs()
                writeText(content)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createDirectory(path: String): Boolean {
        return try {
            File(path).mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    fun delete(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun rename(oldPath: String, newName: String): Boolean {
        return try {
            val oldFile = File(oldPath)
            val newFile = File(oldFile.parent, newName)
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }
}