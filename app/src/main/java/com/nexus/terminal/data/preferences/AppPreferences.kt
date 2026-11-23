package com.nexus.terminal.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_terminal_prefs", Context.MODE_PRIVATE)

    var aiApiKey: String
        get() = prefs.getString("ai_api_key", "") ?: ""
        set(value) = prefs.edit().putString("ai_api_key", value).apply()

    var terminalFontSize: Float
        get() = prefs.getFloat("term_font_size", 13f)
        set(value) = prefs.edit().putFloat("term_font_size", value).apply()

    var editorTabWidth: Int
        get() = prefs.getInt("editor_tab_width", 4)
        set(value) = prefs.edit().putInt("editor_tab_width", value).apply()

    var defaultWorkingDirectory: String
        get() = prefs.getString("default_work_dir", "") ?: ""
        set(value) = prefs.edit().putString("default_work_dir", value).apply()
}
