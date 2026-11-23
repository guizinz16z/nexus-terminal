package com.nexus.terminal.core.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ProcessInfo(
    val pid: String,
    val user: String,
    val command: String
)

object ProcessMonitor {

    suspend fun listProcesses(): List<ProcessInfo> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ProcessInfo>()
        try {
            val pb = ProcessBuilder("ps", "-A")
            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            var line = reader.readLine() // Header

            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.trim().split("\\s+".toRegex())
                if (parts.size >= 4) {
                    val user = parts[0]
                    val pid = parts[1]
                    val cmd = parts.last()
                    list.add(ProcessInfo(pid = pid, user = user, command = cmd))
                }
            }
            proc.waitFor()
        } catch (e: Exception) {
            // Fallback
        }
        list
    }

    suspend fun killProcess(pid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val pb = ProcessBuilder("kill", "-9", pid)
            val proc = pb.start()
            proc.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
