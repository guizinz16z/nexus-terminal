package com.nexus.terminal.core.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Individual terminal session instance in Nexus Terminal.
 */
class TerminalSession(
    val id: String,
    val name: String,
    val workingDir: File
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private var shellProcess: ShellProcess? = null

    private val _outputFlow = MutableStateFlow<CharSequence>("")
    val outputFlow: StateFlow<CharSequence> = _outputFlow

    private val buffer = StringBuilder()
    private val maxBufferLength = 200000

    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1

    init {
        startSession()
    }

    fun startSession() {
        job?.cancel()
        shellProcess?.destroy()

        val process = ShellProcess(workingDir)
        shellProcess = process

        appendOutput("\u001B[1;36m★ NEXUS TERMINAL v1.0.0 Pro\u001B[0m\n")
        appendOutput("\u001B[1;32m✓ Sessão iniciada em: \u001B[0m${workingDir.absolutePath}\n")
        appendOutput("\u001B[1;35m➜ Digite 'help' ou comandos bash/linux.\u001B[0m\n\n")

        job = scope.launch {
            process.start { chunk ->
                appendOutput(chunk)
            }
        }
    }

    private fun appendOutput(text: String) {
        synchronized(buffer) {
            buffer.append(text)
            if (buffer.length > maxBufferLength) {
                buffer.delete(0, buffer.length - maxBufferLength)
            }
            _outputFlow.value = AnsiParser.parse(buffer.toString())
        }
    }

    fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isNotEmpty()) {
            commandHistory.add(trimmed)
            historyIndex = commandHistory.size
        }
        shellProcess?.sendInput(command + "\n")
    }

    fun sendRaw(input: String) {
        shellProcess?.sendInput(input)
    }

    fun sendSigInt() {
        shellProcess?.sendSigInt()
        appendOutput("\n^C\n")
    }

    fun clearBuffer() {
        synchronized(buffer) {
            buffer.clear()
            _outputFlow.value = ""
        }
    }

    fun getPreviousCommand(): String? {
        if (commandHistory.isEmpty()) return null
        if (historyIndex > 0) {
            historyIndex--
            return commandHistory[historyIndex]
        }
        return commandHistory.firstOrNull()
    }

    fun getNextCommand(): String? {
        if (commandHistory.isEmpty()) return null
        if (historyIndex < commandHistory.size - 1) {
            historyIndex++
            return commandHistory[historyIndex]
        }
        historyIndex = commandHistory.size
        return ""
    }

    fun destroy() {
        job?.cancel()
        shellProcess?.destroy()
    }
}
