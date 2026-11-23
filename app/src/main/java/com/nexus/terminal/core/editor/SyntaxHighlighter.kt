package com.nexus.terminal.core.editor

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan

/**
 * Applies syntax highlighting colors to Android Editable.
 */
object SyntaxHighlighter {

    private val COLOR_KEYWORD = Color.parseColor("#C084FC") // Violet
    private val COLOR_BUILTIN = Color.parseColor("#38BDF8") // Cyan
    private val COLOR_STRING = Color.parseColor("#34D399")  // Emerald
    private val COLOR_COMMENT = Color.parseColor("#64748B") // Muted Slate
    private val COLOR_NUMBER = Color.parseColor("#FBBF24")  // Amber
    private val COLOR_FUNCTION = Color.parseColor("#60A5FA")// Blue
    private val COLOR_DECORATOR = Color.parseColor("#F472B6") // Pink

    fun highlight(editable: Editable, language: LanguageType) {
        if (editable.length > 50000) return // Skip very large files for performance

        // Clear existing syntax foreground spans
        val oldSpans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        for (span in oldSpans) {
            editable.removeSpan(span)
        }

        val tokens = when (language) {
            LanguageType.PYTHON -> CodeLexer.tokenizePython(editable)
            else -> CodeLexer.tokenizePython(editable)
        }

        for (token in tokens) {
            val color = when (token.type) {
                TokenType.KEYWORD -> COLOR_KEYWORD
                TokenType.BUILTIN -> COLOR_BUILTIN
                TokenType.STRING -> COLOR_STRING
                TokenType.COMMENT -> COLOR_COMMENT
                TokenType.NUMBER -> COLOR_NUMBER
                TokenType.FUNCTION -> COLOR_FUNCTION
                TokenType.DECORATOR -> COLOR_DECORATOR
                TokenType.OPERATOR -> COLOR_KEYWORD
            }

            editable.setSpan(
                ForegroundColorSpan(color),
                token.start,
                token.end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}
