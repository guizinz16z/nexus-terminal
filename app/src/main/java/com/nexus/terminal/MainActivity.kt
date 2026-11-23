package com.nexus.terminal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nexus.terminal.databinding.ActivityMainBinding
import com.nexus.terminal.ui.dashboard.DashboardFragment
import com.nexus.terminal.ui.editor.EditorFragment
import com.nexus.terminal.ui.files.FilesFragment
import com.nexus.terminal.ui.terminal.TerminalFragment
import com.nexus.terminal.ui.tools.ToolsFragment
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            showFragment(DashboardFragment(), TAG_DASHBOARD)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    showFragment(DashboardFragment(), TAG_DASHBOARD)
                    true
                }
                R.id.nav_terminal -> {
                    showFragment(TerminalFragment(), TAG_TERMINAL)
                    true
                }
                R.id.nav_editor -> {
                    showFragment(EditorFragment(), TAG_EDITOR)
                    true
                }
                R.id.nav_files -> {
                    showFragment(FilesFragment(), TAG_FILES)
                    true
                }
                R.id.nav_tools -> {
                    showFragment(ToolsFragment(), TAG_TOOLS)
                    true
                }
                else -> false
            }
        }
    }

    fun showFragment(fragment: Fragment, tag: String? = null) {
        if (isFinishing || isDestroyed) return
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, tag)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            // Guard against edge-case fragment transaction states
        }
    }

    fun navigateToTerminal() {
        binding.bottomNavigation.selectedItemId = R.id.nav_terminal
    }

    fun navigateToEditor(fileToOpen: File? = null) {
        val editorFrag = EditorFragment.newInstance(fileToOpen?.absolutePath)
        showFragment(editorFrag, TAG_EDITOR)
        binding.bottomNavigation.menu.findItem(R.id.nav_editor)?.isChecked = true
    }

    fun navigateToFiles() {
        binding.bottomNavigation.selectedItemId = R.id.nav_files
    }

    companion object {
        const val TAG_DASHBOARD = "frag_dashboard"
        const val TAG_TERMINAL = "frag_terminal"
        const val TAG_EDITOR = "frag_editor"
        const val TAG_FILES = "frag_files"
        const val TAG_TOOLS = "frag_tools"
    }
}
