package com.nexus.terminal.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * AI Assistant Service for Nexus Terminal.
 * Connects to user-configured AI APIs (OpenAI, Gemini, OpenRouter, Groq).
 */
class AiAssistantService(
    private var apiKey: String = "",
    private var endpointUrl: String = "https://api.openai.com/v1/chat/completions"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setApiKey(key: String) {
        apiKey = key.trim()
    }

    fun setEndpointUrl(url: String) {
        endpointUrl = url.trim()
    }

    suspend fun askAssistant(prompt: String, contextCode: String = ""): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) {
            return@withContext """
                ★ Nexus AI Copilot (Modo Offline)
                
                Nenhuma chave de API configurada no momento.
                Para ativar a inteligência artificial completa:
                1. Toque em 'Chave API' no canto superior direito.
                2. Insira sua chave de API (OpenAI, Gemini ou OpenRouter).
                
                Dica Rápida para o seu código:
                Para executar scripts em segundo plano no terminal, use:
                  python3 main.py &
                Para verificar processos em execução, acesse a aba 'Ferramentas' -> 'Process Monitor'.
            """.trimIndent()
        }

        try {
            val systemMessage = "Você é o assistente inteligente do aplicativo NEXUS TERMINAL para Android. Ajude o desenvolvedor com códigos Python, comandos bash/Linux, depuração de erros e otimizações. Seja direto, técnico e conciso."

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemMessage)
                })
                if (contextCode.isNotEmpty()) {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Contexto de código:\n```\n$contextCode\n```\n\n$prompt")
                    })
                } else {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
            }

            val requestJson = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", messagesArray)
                put("max_tokens", 1000)
                put("temperature", 0.7)
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpointUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Erro na API AI (Código ${response.code}): ${response.message}"
                }
                val respBody = response.body?.string() ?: return@withContext "Resposta vazia da API."
                val json = JSONObject(respBody)
                val choices = json.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    message.getString("content")
                } else {
                    "Nenhuma resposta gerada."
                }
            }
        } catch (e: Exception) {
            "Erro de conexão com assistente AI: ${e.message}"
        }
    }
}
