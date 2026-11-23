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

    private val dashboardFragment = DashboardFragment()
    private val terminalFragment = TerminalFragment()
    private val editorFragment = EditorFragment()
    private val filesFragment = FilesFragment()
    private val toolsFragment = ToolsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            showFragment(dashboardFragment)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    showFragment(dashboardFragment)
                    true
                }
                R.id.nav_terminal -> {
                    showFragment(terminalFragment)
                    true
                }
                R.id.nav_editor -> {
                    showFragment(editorFragment)
                    true
                }
                R.id.nav_files -> {
                    showFragment(filesFragment)
                    true
                }
                R.id.nav_tools -> {
                    showFragment(toolsFragment)
                    true
                }
                else -> false
            }
        }
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    fun navigateToTerminal() {
        binding.bottomNavigation.selectedItemId = R.id.nav_terminal
    }

    fun navigateToEditor(fileToOpen: File? = null) {
        if (fileToOpen != null) {
            editorFragment.openFile(fileToOpen)
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_editor
    }

    fun navigateToFiles() {
        binding.bottomNavigation.selectedItemId = R.id.nav_files
    }
}
