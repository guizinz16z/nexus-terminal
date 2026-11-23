package com.nexus.terminal.core.storage

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified()
) {
    val extension: String
        get() = if (isDirectory) "" else file.extension.lowercase()

    val formattedSize: String
        get() {
            if (isDirectory) {
                val childCount = file.list()?.size ?: 0
                return "$childCount itens"
            }
            if (size < 1024) return "$size B"
            val kb = size / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format(Locale.US, "%.1f MB", mb)
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}
