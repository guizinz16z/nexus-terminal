package com.nexus.terminal.core.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Git VCS engine for Nexus Terminal.
 * Executes git operations using the local git binary or shell.
 */
object GitManager {

    suspend fun getStatus(repoDir: File): GitStatus = withContext(Dispatchers.IO) {
        val branch = executeGitCommand(repoDir, listOf("branch", "--show-current")).trim()
            .ifEmpty { "main" }

        val porcelainOutput = executeGitCommand(repoDir, listOf("status", "--porcelain"))
        val modified = mutableListOf<String>()
        val untracked = mutableListOf<String>()

        porcelainOutput.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("??")) {
                untracked.add(trimmed.substring(2).trim())
            } else if (trimmed.isNotEmpty()) {
                modified.add(trimmed)
            }
        }

        GitStatus(
            branch = branch,
            modifiedFiles = modified,
            untrackedFiles = untracked
        )
    }

    suspend fun commit(repoDir: File, message: String): String = withContext(Dispatchers.IO) {
        executeGitCommand(repoDir, listOf("add", "."))
        executeGitCommand(repoDir, listOf("commit", "-m", message))
    }

    suspend fun clone(
        repoUrl: String,
        targetDir: File,
        onLog: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        onLog("Clonando repositório: $repoUrl...\n")
        try {
            val pb = ProcessBuilder("git", "clone", repoUrl, targetDir.absolutePath)
            pb.redirectErrorStream(true)
            val proc = pb.start()

            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                onLog("$line\n")
            }
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                onLog("✓ Clone concluído com sucesso em ${targetDir.name}!\n")
                true
            } else {
                onLog("✗ Falha no clone (código: $exitCode)\n")
                false
            }
        } catch (e: Exception) {
            onLog("Erro ao executar git clone: ${e.message}\n")
            false
        }
    }

    suspend fun pull(repoDir: File): String = withContext(Dispatchers.IO) {
        executeGitCommand(repoDir, listOf("pull"))
    }

    suspend fun push(repoDir: File): String = withContext(Dispatchers.IO) {
        executeGitCommand(repoDir, listOf("push"))
    }

    suspend fun getLog(repoDir: File, limit: Int = 10): List<String> = withContext(Dispatchers.IO) {
        val output = executeGitCommand(repoDir, listOf("log", "--oneline", "-n", limit.toString()))
        output.lines().filter { it.isNotBlank() }
    }

    private fun executeGitCommand(dir: File, args: List<String>): String {
        return try {
            val cmd = mutableListOf("git")
            cmd.addAll(args)
            val pb = ProcessBuilder(cmd)
            pb.directory(dir)
            pb.redirectErrorStream(true)
            val proc = pb.start()
            val output = proc.inputStream.bufferedReader().readText()
            proc.waitFor()
            output
        } catch (e: Exception) {
            "Erro git: ${e.message}"
        }
    }
}
