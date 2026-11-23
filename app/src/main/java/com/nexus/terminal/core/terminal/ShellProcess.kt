package com.nexus.terminal.core.terminal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream

/**
 * Shell Process execution engine for Nexus Terminal.
 * Manages native process lifecycle, streams, environment variables,
 * working directories, and process interruption (SIGINT).
 */
class ShellProcess(
    private val workingDir: File,
    private val customEnv: Map<String, String> = emptyMap()
) {
    private var process: Process? = null
    private var outputStream: OutputStream? = null
    private var isRunning: Boolean = false

    suspend fun start(
        command: List<String> = detectDefaultShell(),
        onOutput: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            if (!workingDir.exists()) {
                workingDir.mkdirs()
            }
            val tmpDir = File(workingDir, "tmp").apply { if (!exists()) mkdirs() }

            val processBuilder = ProcessBuilder(command)
            processBuilder.directory(workingDir)
            processBuilder.redirectErrorStream(true)

            // Setup comprehensive Android / Linux environment
            val env = processBuilder.environment()
            val currentPath = env["PATH"] ?: "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin"
            val customBinPath = "${workingDir.absolutePath}/bin:/data/data/com.nexus.terminal/files/bin"
            env["PATH"] = "$customBinPath:$currentPath"
            env["HOME"] = workingDir.absolutePath
            env["TMPDIR"] = tmpDir.absolutePath
            env["TERM"] = "xterm-256color"
            env["LANG"] = "en_US.UTF-8"
            env["LC_ALL"] = "en_US.UTF-8"
            env["COLORTERM"] = "truecolor"

            // Apply custom environment variables
            for ((key, value) in customEnv) {
                env[key] = value
            }

            val proc = processBuilder.start()
            process = proc
            outputStream = proc.outputStream
            isRunning = true

            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val buffer = CharArray(1024)
            var charsRead = 0

            while (isRunning) {
                try {
                    charsRead = reader.read(buffer)
                    if (charsRead == -1) break
                    val output = String(buffer, 0, charsRead)
                    onOutput(output)
                } catch (e: Exception) {
                    break
                }
            }

            val exitCode = try { proc.waitFor() } catch (e: Exception) { 0 }
            isRunning = false
            onOutput("\n[Processo finalizado com código: $exitCode]\n")
        } catch (e: Exception) {
            isRunning = false
            onOutput("\n[Erro de execução: ${e.message}]\n")
        }
    }

    fun sendInput(input: String) {
        try {
            outputStream?.let {
                it.write(input.toByteArray(Charsets.UTF_8))
                it.flush()
            }
        } catch (e: Exception) {
            // Ignore broken pipe if process terminated
        }
    }

    fun sendSigInt() {
        try {
            outputStream?.let {
                it.write(byteArrayOf(3))
                it.flush()
            }
        } catch (e: Exception) {
            destroy()
        }
    }

    fun destroy() {
        isRunning = false
        try {
            process?.destroy()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun isAlive(): Boolean {
        return isRunning && (process?.isAlive ?: false)
    }

    companion object {
        fun detectDefaultShell(): List<String> {
            val candidates = listOf("/bin/bash", "/bin/sh", "/system/bin/sh")
            for (path in candidates) {
                val f = File(path)
                if (f.exists() && f.canExecute()) {
                    return listOf(path)
                }
            }
            return listOf("sh")
        }
    }
}
