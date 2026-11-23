package com.nexus.terminal.core.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

object NetworkUtils {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun pingHost(host: String): String = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        try {
            val pb = ProcessBuilder("ping", "-c", "4", cleanHost)
            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            proc.waitFor()
            if (sb.isEmpty()) "Falha ao executar ping em $cleanHost" else sb.toString()
        } catch (e: Exception) {
            "Erro de ping: ${e.message}"
        }
    }

    suspend fun executeHttpRequest(url: String): String = withContext(Dispatchers.IO) {
        val targetUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else {
            url
        }

        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder().url(targetUrl).build()
            client.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val body = response.body?.string() ?: ""
                """
                    HTTP / ${response.code} ${response.message}
                    Tempo: ${duration}ms
                    Tamanho: ${body.length} bytes
                    
                    Headers:
                    ${response.headers}
                    
                    Corpo da Resposta:
                    $body
                """.trimIndent()
            }
        } catch (e: Exception) {
            "Erro HTTP: ${e.message}"
        }
    }

    suspend fun checkPort(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 2000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
