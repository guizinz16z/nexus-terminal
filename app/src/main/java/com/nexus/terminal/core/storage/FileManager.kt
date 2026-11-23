package com.nexus.terminal.core.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * File System Manager for Nexus Terminal.
 */
object FileManager {

    suspend fun listFiles(directory: File): List<FileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }
        val files = directory.listFiles() ?: return@withContext emptyList()
        files.map { FileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    suspend fun createFile(parent: File, fileName: String, initialContent: String = ""): File = withContext(Dispatchers.IO) {
        val target = File(parent, fileName)
        if (!target.exists()) {
            target.createNewFile()
        }
        if (initialContent.isNotEmpty()) {
            target.writeText(initialContent, Charsets.UTF_8)
        }
        target
    }

    suspend fun createFolder(parent: File, folderName: String): File = withContext(Dispatchers.IO) {
        val target = File(parent, folderName)
        if (!target.exists()) {
            target.mkdirs()
        }
        target
    }

    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    suspend fun renameFile(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        val dest = File(file.parentFile, newName)
        file.renameTo(dest)
    }

    suspend fun readText(file: File): String = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext ""
        file.readText(Charsets.UTF_8)
    }

    suspend fun writeText(file: File, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            file.writeText(content, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun copy(source: File, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val target = File(destinationDir, source.name)
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = true)
            } else {
                source.copyTo(target, overwrite = true)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun move(source: File, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val target = File(destinationDir, source.name)
            source.renameTo(target)
        } catch (e: Exception) {
            false
        }
    }
}
