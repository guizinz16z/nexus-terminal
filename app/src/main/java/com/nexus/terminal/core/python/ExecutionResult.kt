package com.nexus.terminal.core.python

data class ExecutionResult(
    val exitCode: Int,
    val output: String,
    val error: String,
    val durationMs: Long,
    val success: Boolean = exitCode == 0
)
