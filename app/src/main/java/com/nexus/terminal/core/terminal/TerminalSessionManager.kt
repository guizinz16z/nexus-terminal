package com.nexus.terminal.core.terminal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID

/**
 * Singleton / Manager for multi-session terminal tabs.
 */
object TerminalSessionManager {

    private val sessions = mutableListOf<TerminalSession>()
    private val _sessionsFlow = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessionsFlow: StateFlow<List<TerminalSession>> = _sessionsFlow

    private val _activeSessionFlow = MutableStateFlow<TerminalSession?>(null)
    val activeSessionFlow: StateFlow<TerminalSession?> = _activeSessionFlow

    fun initDefaultSession(defaultDir: File) {
        if (sessions.isEmpty()) {
            createNewSession(defaultDir, "Sessão 1")
        }
    }

    fun createNewSession(workingDir: File, name: String? = null): TerminalSession {
        val sessionName = name ?: "Sessão ${sessions.size + 1}"
        val session = TerminalSession(
            id = UUID.randomUUID().toString(),
            name = sessionName,
            workingDir = workingDir
        )
        sessions.add(session)
        _sessionsFlow.value = sessions.toList()
        _activeSessionFlow.value = session
        return session
    }

    fun selectSession(session: TerminalSession) {
        if (sessions.contains(session)) {
            _activeSessionFlow.value = session
        }
    }

    fun closeSession(session: TerminalSession) {
        session.destroy()
        sessions.remove(session)
        _sessionsFlow.value = sessions.toList()
        if (_activeSessionFlow.value == session) {
            _activeSessionFlow.value = sessions.lastOrNull()
        }
    }

    fun closeAll() {
        for (session in sessions) {
            session.destroy()
        }
        sessions.clear()
        _sessionsFlow.value = emptyList()
        _activeSessionFlow.value = null
    }
}
