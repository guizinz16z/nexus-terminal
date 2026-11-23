package com.nexus.terminal.core.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * High-performance ZIP compression and extraction utility.
 */
object ZipUtils {

    suspend fun zipDirectory(sourceDir: File, zipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!sourceDir.exists()) return@withContext false
            if (zipFile.parentFile?.exists() == false) {
                zipFile.parentFile?.mkdirs()
            }

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                zipRecursive(sourceDir, sourceDir, zos)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun zipRecursive(rootDir: File, currentFile: File, zos: ZipOutputStream) {
        val files = currentFile.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                zipRecursive(rootDir, file, zos)
            } else {
                val relativePath = rootDir.toURI().relativize(file.toURI()).path
                val entry = ZipEntry(relativePath)
                zos.putNextEntry(entry)
                FileInputStream(file).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    suspend fun unzip(zipFile: File, destDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!zipFile.exists()) return@withContext false
            if (!destDir.exists()) destDir.mkdirs()

            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(2048)

                while (entry != null) {
                    val newFile = File(destDir, entry.name)
                    // Prevent zip-slip vulnerability
                    if (!newFile.canonicalPath.startsWith(destDir.canonicalPath)) {
                        throw SecurityException("Zip entry is outside of target dir: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
