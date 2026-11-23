package com.nexus.terminal.core.project

import java.io.File

enum class ProjectTemplateType(val displayName: String) {
    TELEGRAM_BOT("Telegram Bot"),
    FASTAPI_SERVER("API Web Server"),
    AUTOMATION_SCRIPT("Script de Automação"),
    CLI_TOOL("Ferramenta CLI"),
    DATA_SCIENCE("Data Science & Math"),
    EMPTY("Projeto Vazio")
}

data class ProjectModel(
    val id: String,
    val name: String,
    val path: String,
    val templateType: ProjectTemplateType,
    val entryPoint: String = "main.py",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    val projectDir: File
        get() = File(path)

    val entryPointFile: File
        get() = File(projectDir, entryPoint)
}
