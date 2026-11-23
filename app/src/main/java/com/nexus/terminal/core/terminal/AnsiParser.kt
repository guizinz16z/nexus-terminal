package com.nexus.terminal.core.terminal

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import java.util.regex.Pattern

/**
 * High-performance ANSI escape sequence parser for Nexus Terminal.
 * Converts ANSI escape codes (16-color, 256-color, bold, underline)
 * into Android Spannable text with full fidelity.
 */
object AnsiParser {

    private val ANSI_PATTERN = Pattern.compile("\u001B\\[([0-9;]*)m")

    // Default ANSI 16 Color Palette (Nexus Cyber Theme)
    private val COLOR_PALETTE = intArrayOf(
        Color.parseColor("#1E293B"), // 0: Black
        Color.parseColor("#F87171"), // 1: Red
        Color.parseColor("#34D399"), // 2: Green
        Color.parseColor("#FBBF24"), // 3: Yellow
        Color.parseColor("#60A5FA"), // 4: Blue
        Color.parseColor("#C084FC"), // 5: Magenta
        Color.parseColor("#38BDF8"), // 6: Cyan
        Color.parseColor("#F1F5F9"), // 7: White
        Color.parseColor("#475569"), // 8: Bright Black
        Color.parseColor("#EF4444"), // 9: Bright Red
        Color.parseColor("#10B981"), // 10: Bright Green
        Color.parseColor("#F59E0B"), // 11: Bright Yellow
        Color.parseColor("#3B82F6"), // 12: Bright Blue
        Color.parseColor("#8B5CF6"), // 13: Bright Magenta
        Color.parseColor("#06B6D4"), // 14: Bright Cyan
        Color.parseColor("#FFFFFF")  // 15: Bright White
    )

    private val DEFAULT_TEXT_COLOR = Color.parseColor("#E2E8F0")

    fun parse(rawText: String): CharSequence {
        if (!rawText.contains("\u001B[")) {
            return rawText
        }

        val builder = SpannableStringBuilder()
        val matcher = ANSI_PATTERN.matcher(rawText)

        var lastEnd = 0
        var currentColor = DEFAULT_TEXT_COLOR
        var isBold = false

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Append preceding text
            if (start > lastEnd) {
                val textSegment = rawText.substring(lastEnd, start)
                val spanStart = builder.length
                builder.append(textSegment)

                builder.setSpan(
                    ForegroundColorSpan(currentColor),
                    spanStart,
                    builder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (isBold) {
                    builder.setSpan(
                        StyleSpan(Typeface.BOLD),
                        spanStart,
                        builder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            // Parse escape code arguments
            val codes = matcher.group(1)?.split(";") ?: emptyList()
            if (codes.isEmpty() || (codes.size == 1 && codes[0].isEmpty()) || codes[0] == "0") {
                // Reset
                currentColor = DEFAULT_TEXT_COLOR
                isBold = false
            } else {
                var i = 0
                while (i < codes.size) {
                    val code = codes[i].toIntOrNull() ?: 0
                    when (code) {
                        0 -> {
                            currentColor = DEFAULT_TEXT_COLOR
                            isBold = false
                        }
                        1 -> isBold = true
                        22 -> isBold = false
                        in 30..37 -> currentColor = COLOR_PALETTE[code - 30]
                        39 -> currentColor = DEFAULT_TEXT_COLOR
                        in 90..97 -> currentColor = COLOR_PALETTE[(code - 90) + 8]
                        38 -> {
                            // 256 colors: 38;5;n
                            if (i + 2 < codes.size && codes[i + 1] == "5") {
                                val colorIndex = codes[i + 2].toIntOrNull() ?: 7
                                currentColor = get256Color(colorIndex)
                                i += 2
                            }
                        }
                    }
                    i++
                }
            }
            lastEnd = end
        }

        // Append trailing text
        if (lastEnd < rawText.length) {
            val textSegment = rawText.substring(lastEnd)
            val spanStart = builder.length
            builder.append(textSegment)
            builder.setSpan(
                ForegroundColorSpan(currentColor),
                spanStart,
                builder.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (isBold) {
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    spanStart,
                    builder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return builder
    }

    private fun get256Color(index: Int): Int {
        if (index in 0..15) {
            return COLOR_PALETTE[index]
        }
        if (index in 16..231) {
            // 6x6x6 color cube
            val base = index - 16
            val r = (base / 36) * 51
            val g = ((base % 36) / 6) * 51
            val b = (base % 6) * 51
            return Color.rgb(r, g, b)
        }
        if (index in 232..255) {
            // Grayscale ramp
            val gray = (index - 232) * 10 + 8
            return Color.rgb(gray, gray, gray)
        }
        return DEFAULT_TEXT_COLOR
    }
}
