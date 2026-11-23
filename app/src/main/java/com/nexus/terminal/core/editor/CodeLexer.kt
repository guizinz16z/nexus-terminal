package com.nexus.terminal.core.editor

import java.util.regex.Pattern

data class CodeToken(
    val start: Int,
    val end: Int,
    val type: TokenType
)

enum class TokenType {
    KEYWORD,
    BUILTIN,
    STRING,
    COMMENT,
    NUMBER,
    FUNCTION,
    DECORATOR,
    OPERATOR
}

/**
 * Fast Regex-based syntax lexer for code editor.
 */
object CodeLexer {

    private val PYTHON_KEYWORDS = Pattern.compile(
        "\\b(def|class|return|if|elif|else|for|while|import|from|as|try|except|finally|with|lambda|yield|async|await|pass|break|continue|in|is|not|and|or|None|True|False|raise|global|nonlocal|assert)\\b"
    )

    private val PYTHON_BUILTINS = Pattern.compile(
        "\\b(print|len|range|enumerate|zip|map|filter|int|str|float|bool|list|dict|set|tuple|open|type|isinstance|sum|min|max|super|any|all|abs|round|id|dir|vars|input)\\b"
    )

    private val PYTHON_STRINGS = Pattern.compile(
        "(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*')"
    )

    private val PYTHON_COMMENTS = Pattern.compile("#.*$")

    private val PYTHON_NUMBERS = Pattern.compile("\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b")

    private val PYTHON_DECORATORS = Pattern.compile("@[a-zA-Z_]\\w*")

    private val PYTHON_FUNCTIONS = Pattern.compile("(?<=def\\s+)[a-zA-Z_]\\w*")

    fun tokenizePython(text: CharSequence): List<CodeToken> {
        val tokens = mutableListOf<CodeToken>()

        // Strings first
        val strMatcher = PYTHON_STRINGS.matcher(text)
        while (strMatcher.find()) {
            tokens.add(CodeToken(strMatcher.start(), strMatcher.end(), TokenType.STRING))
        }

        // Comments
        val comMatcher = PYTHON_COMMENTS.matcher(text)
        while (comMatcher.find()) {
            tokens.add(CodeToken(comMatcher.start(), comMatcher.end(), TokenType.COMMENT))
        }

        // Keywords
        val kwMatcher = PYTHON_KEYWORDS.matcher(text)
        while (kwMatcher.find()) {
            if (!isInExistingTokens(kwMatcher.start(), kwMatcher.end(), tokens)) {
                tokens.add(CodeToken(kwMatcher.start(), kwMatcher.end(), TokenType.KEYWORD))
            }
        }

        // Builtins
        val biMatcher = PYTHON_BUILTINS.matcher(text)
        while (biMatcher.find()) {
            if (!isInExistingTokens(biMatcher.start(), biMatcher.end(), tokens)) {
                tokens.add(CodeToken(biMatcher.start(), biMatcher.end(), TokenType.BUILTIN))
            }
        }

        // Functions
        val fnMatcher = PYTHON_FUNCTIONS.matcher(text)
        while (fnMatcher.find()) {
            if (!isInExistingTokens(fnMatcher.start(), fnMatcher.end(), tokens)) {
                tokens.add(CodeToken(fnMatcher.start(), fnMatcher.end(), TokenType.FUNCTION))
            }
        }

        // Decorators
        val decMatcher = PYTHON_DECORATORS.matcher(text)
        while (decMatcher.find()) {
            if (!isInExistingTokens(decMatcher.start(), decMatcher.end(), tokens)) {
                tokens.add(CodeToken(decMatcher.start(), decMatcher.end(), TokenType.DECORATOR))
            }
        }

        // Numbers
        val numMatcher = PYTHON_NUMBERS.matcher(text)
        while (numMatcher.find()) {
            if (!isInExistingTokens(numMatcher.start(), numMatcher.end(), tokens)) {
                tokens.add(CodeToken(numMatcher.start(), numMatcher.end(), TokenType.NUMBER))
            }
        }

        return tokens
    }

    private fun isInExistingTokens(start: Int, end: Int, tokens: List<CodeToken>): Boolean {
        for (token in tokens) {
            if (start >= token.start && end <= token.end) {
                return true
            }
        }
        return false
    }
}
