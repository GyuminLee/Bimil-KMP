package com.imbavchenko.bimil.data.encryption

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

actual class HashService actual constructor() {
    private val secureRandom = SecureRandom()

    actual fun generateSalt(): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    actual fun hashPin(pin: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val combined = saltBytes + pin.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined)
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
