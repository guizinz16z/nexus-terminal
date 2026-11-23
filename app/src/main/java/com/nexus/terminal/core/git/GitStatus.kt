package com.nexus.terminal.core.git

data class GitStatus(
    val branch: String,
    val modifiedFiles: List<String>,
    val untrackedFiles: List<String>,
    val isClean: Boolean = modifiedFiles.isEmpty() && untrackedFiles.isEmpty()
)
