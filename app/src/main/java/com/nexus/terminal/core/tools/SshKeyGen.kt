package com.nexus.terminal.core.tools

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPairGenerator

data class SshKeyPair(
    val publicKey: String,
    val privateKey: String
)

object SshKeyGen {

    suspend fun generateRsaKeyPair(bits: Int = 2048, comment: String = "nexus@android"): SshKeyPair = withContext(Dispatchers.IO) {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(bits)
        val pair = keyGen.generateKeyPair()

        val pubEncoded = pair.public.encoded
        val privEncoded = pair.private.encoded

        val pubBase64 = Base64.encodeToString(pubEncoded, Base64.NO_WRAP)
        val privBase64 = Base64.encodeToString(privEncoded, Base64.NO_WRAP)

        val pubFormatted = "ssh-rsa $pubBase64 $comment"
        val privFormatted = """
            -----BEGIN RSA PRIVATE KEY-----
            $privBase64
            -----END RSA PRIVATE KEY-----
        """.trimIndent()

        SshKeyPair(publicKey = pubFormatted, privateKey = privFormatted)
    }
}
