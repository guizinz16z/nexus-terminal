package com.nexus.terminal

import android.app.Application
import android.util.Log
import com.nexus.terminal.core.ai.AiAssistantService
import com.nexus.terminal.core.project.ProjectManager
import com.nexus.terminal.core.project.ProjectTemplateType
import com.nexus.terminal.core.python.PipManager
import com.nexus.terminal.core.python.PythonEngine
import com.nexus.terminal.core.terminal.TerminalSessionManager
import com.nexus.terminal.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class NexusApplication : Application() {

    lateinit var preferences: AppPreferences
        private set

    lateinit var pythonEngine: PythonEngine
        private set

    lateinit var pipManager: PipManager
        private set

    lateinit var projectManager: ProjectManager
        private set

    lateinit var aiAssistantService: AiAssistantService
        private set

    val workspaceDir: File by lazy {
        val external = File("/workspace")
        if (external.exists() && external.canWrite()) {
            external
        } else {
            File(filesDir, "workspace").apply { if (!exists()) mkdirs() }
        }
    }

    val projectsDir: File by lazy {
        File(workspaceDir, "projects").apply { if (!exists()) mkdirs() }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        setupCrashHandler()

        // Ensure essential directories exist
        workspaceDir.mkdirs()
        projectsDir.mkdirs()
        File(workspaceDir, "tmp").mkdirs()
        File(workspaceDir, "bin").mkdirs()

        preferences = AppPreferences(this)
        pythonEngine = PythonEngine(filesDir)
        pipManager = PipManager(pythonEngine, pythonEngine.userSitePackages)
        projectManager = ProjectManager(projectsDir)
        aiAssistantService = AiAssistantService(apiKey = preferences.aiApiKey)

        // Initialize default terminal session safely
        try {
            TerminalSessionManager.initDefaultSession(workspaceDir)
        } catch (e: Exception) {
            Log.e("NexusApp", "Failed to init default terminal session", e)
        }

        // Bootstrap initial welcome project if empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = projectManager.listProjects()
                if (existing.isEmpty()) {
                    projectManager.createProject(
                        name = "Nexus_QuickStart",
                        template = ProjectTemplateType.TELEGRAM_BOT,
                        description = "Projeto inicial de demonstração do Nexus Terminal."
                    )
                }
            } catch (e: Exception) {
                Log.e("NexusApp", "Failed to bootstrap quickstart project", e)
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stackTrace = sw.toString()
                Log.e("NexusCrash", "FATAL EXCEPTION in thread ${thread.name}: $stackTrace")

                val crashFile = File(filesDir, "crash_log.txt")
                crashFile.writeText("Time: ${System.currentTimeMillis()}\nThread: ${thread.name}\n$stackTrace")
            } catch (e: Exception) {
                // Prevent crash loop in handler
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        lateinit var instance: NexusApplication
            private set
    }
}
