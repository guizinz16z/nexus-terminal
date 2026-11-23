package com.nexus.terminal.core.editor

enum class LanguageType {
    PYTHON,
    SHELL,
    JSON,
    MARKDOWN,
    JAVASCRIPT,
    HTML,
    CSS,
    TEXT;

    companion object {
        fun fromFileName(fileName: String): LanguageType {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "py", "pyw" -> PYTHON
                "sh", "bash", "zsh" -> SHELL
                "json" -> JSON
                "md", "markdown" -> MARKDOWN
                "js", "mjs" -> JAVASCRIPT
                "html", "htm" -> HTML
                "css" -> CSS
                else -> TEXT
            }
        }
    }
}
