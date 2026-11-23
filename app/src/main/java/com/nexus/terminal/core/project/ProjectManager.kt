package com.nexus.terminal.core.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Project Manager: handles project creation, persistence and templates.
 */
class ProjectManager(
    private val projectsRootDir: File
) {
    init {
        if (!projectsRootDir.exists()) {
            projectsRootDir.mkdirs()
        }
    }

    suspend fun listProjects(): List<ProjectModel> = withContext(Dispatchers.IO) {
        val projects = mutableListOf<ProjectModel>()
        val dirs = projectsRootDir.listFiles { f -> f.isDirectory } ?: return@withContext emptyList()

        for (dir in dirs) {
            val metaFile = File(dir, "nexus-project.json")
            if (metaFile.exists()) {
                try {
                    val json = JSONObject(metaFile.readText(Charsets.UTF_8))
                    val templateName = json.optString("template", ProjectTemplateType.EMPTY.name)
                    val template = try {
                        ProjectTemplateType.valueOf(templateName)
                    } catch (e: Exception) {
                        ProjectTemplateType.EMPTY
                    }

                    projects.add(
                        ProjectModel(
                            id = json.optString("id", dir.name),
                            name = json.optString("name", dir.name),
                            path = dir.absolutePath,
                            templateType = template,
                            entryPoint = json.optString("entryPoint", "main.py"),
                            description = json.optString("description", ""),
                            createdAt = json.optLong("createdAt", dir.lastModified()),
                            lastModified = dir.lastModified()
                        )
                    )
                } catch (e: Exception) {
                    // Fallback to basic project model
                    projects.add(
                        ProjectModel(
                            id = dir.name,
                            name = dir.name,
                            path = dir.absolutePath,
                            templateType = ProjectTemplateType.EMPTY,
                            lastModified = dir.lastModified()
                        )
                    )
                }
            }
        }
        projects.sortedByDescending { it.lastModified }
    }

    suspend fun createProject(
        name: String,
        template: ProjectTemplateType,
        description: String = ""
    ): ProjectModel = withContext(Dispatchers.IO) {
        val safeName = name.trim().replace("\\s+".toRegex(), "_")
        val projectDir = File(projectsRootDir, safeName)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val id = UUID.randomUUID().toString()
        val meta = JSONObject().apply {
            put("id", id)
            put("name", safeName)
            put("template", template.name)
            put("entryPoint", "main.py")
            put("description", description)
            put("createdAt", System.currentTimeMillis())
        }

        File(projectDir, "nexus-project.json").writeText(meta.toString(2), Charsets.UTF_8)

        // Write template files
        val files = ProjectTemplates.getFilesForTemplate(template, safeName)
        for ((fileName, content) in files) {
            File(projectDir, fileName).writeText(content, Charsets.UTF_8)
        }

        ProjectModel(
            id = id,
            name = safeName,
            path = projectDir.absolutePath,
            templateType = template,
            description = description
        )
    }

    suspend fun deleteProject(project: ProjectModel): Boolean = withContext(Dispatchers.IO) {
        val dir = File(project.path)
        if (dir.exists()) {
            dir.deleteRecursively()
        } else {
            false
        }
    }
}
