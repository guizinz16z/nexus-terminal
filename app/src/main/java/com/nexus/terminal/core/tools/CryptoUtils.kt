package com.nexus.terminal.core.tools

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest

object CryptoUtils {

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun base64Encode(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun base64Decode(input: String): String {
        return try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            String(decoded, Charsets.UTF_8)
        } catch (e: Exception) {
            "Erro ao decodificar Base64"
        }
    }

    fun urlEncode(input: String): String {
        return URLEncoder.encode(input, "UTF-8")
    }

    fun urlDecode(input: String): String {
        return URLDecoder.decode(input, "UTF-8")
    }
}
