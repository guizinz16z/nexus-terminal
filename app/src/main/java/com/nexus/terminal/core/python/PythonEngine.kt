package com.nexus.terminal.core.python

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Real Python Execution Engine for Nexus Terminal.
 * Locates the Python interpreter, configures PYTHONPATH and site-packages,
 * handles unbuffered stdout/stderr streaming, and supports cancellation.
 */
class PythonEngine(
    private val appFilesDir: File
) {
    private var currentProcess: Process? = null
    private var isCancelled = false

    val userSitePackages: File by lazy {
        File(appFilesDir, "site-packages").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun executeScript(
        scriptFile: File,
        workingDir: File = scriptFile.parentFile ?: appFilesDir,
        onChunk: (String) -> Unit
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val pythonBin = detectPythonBinary()

        val pb = ProcessBuilder(pythonBin, "-u", scriptFile.absolutePath)
        pb.directory(workingDir)
        pb.redirectErrorStream(false)

        val env = pb.environment()
        val currentPyPath = env["PYTHONPATH"] ?: ""
        env["PYTHONPATH"] = "${userSitePackages.absolutePath}:${workingDir.absolutePath}:$currentPyPath"
        env["PYTHONUNBUFFERED"] = "1"
        env["HOME"] = appFilesDir.absolutePath
        env["TMPDIR"] = "${appFilesDir.absolutePath}/tmp"
        env["LANG"] = "en_US.UTF-8"

        isCancelled = false
        val outputAcc = StringBuilder()
        val errorAcc = StringBuilder()

        try {
            val proc = pb.start()
            currentProcess = proc

            val outThread = Thread {
                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val formatted = "$line\n"
                    outputAcc.append(formatted)
                    onChunk(formatted)
                }
            }

            val errThread = Thread {
                val reader = BufferedReader(InputStreamReader(proc.errorStream))
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val formatted = "[stderr] $line\n"
                    errorAcc.append(formatted)
                    onChunk(formatted)
                }
            }

            outThread.start()
            errThread.start()

            val exitCode = proc.waitFor()
            outThread.join(2000)
            errThread.join(2000)

            val duration = System.currentTimeMillis() - startTime
            ExecutionResult(
                exitCode = exitCode,
                output = outputAcc.toString(),
                error = errorAcc.toString(),
                durationMs = duration
            )
        } catch (e: Exception) {
            val msg = "\n[Erro de execução: ${e.message}]\n"
            onChunk(msg)
            ExecutionResult(
                exitCode = -1,
                output = outputAcc.toString(),
                error = e.message ?: "Unknown error",
                durationMs = System.currentTimeMillis() - startTime
            )
        } finally {
            currentProcess = null
        }
    }

    suspend fun executeInlineCode(
        code: String,
        workingDir: File = appFilesDir,
        onChunk: (String) -> Unit
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val tempScript = File(appFilesDir, "nexus_inline_exec.py")
        try {
            tempScript.writeText(code)
            executeScript(tempScript, workingDir, onChunk)
        } finally {
            if (tempScript.exists()) {
                tempScript.delete()
            }
        }
    }

    fun stopExecution() {
        isCancelled = true
        try {
            currentProcess?.destroy()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun detectPythonBinary(): String {
        val candidates = listOf(
            "/usr/bin/python3",
            "/usr/bin/python",
            "/system/bin/python3",
            "/data/data/com.nexus.terminal/files/usr/bin/python3",
            "python3",
            "python"
        )
        for (candidate in candidates) {
            try {
                if (candidate.startsWith("/")) {
                    val f = File(candidate)
                    if (f.exists() && f.canExecute()) {
                        return candidate
                    }
                } else {
                    return candidate
                }
            } catch (e: Exception) {
                // Next
            }
        }
        return "python3"
    }

    fun getPythonVersion(): String {
        return try {
            val pb = ProcessBuilder(detectPythonBinary(), "--version")
            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val line = reader.readLine() ?: "Python 3.11+"
            proc.waitFor()
            line
        } catch (e: Exception) {
            "Python 3.11 (Nexus Runtime)"
        }
    }
}
