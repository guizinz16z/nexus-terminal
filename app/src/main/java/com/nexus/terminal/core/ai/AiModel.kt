package com.nexus.terminal.core.ai

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
