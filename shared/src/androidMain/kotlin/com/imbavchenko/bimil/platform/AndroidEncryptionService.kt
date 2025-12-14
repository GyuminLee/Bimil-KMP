package com.imbavchenko.bimil.data.encryption

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual class EncryptionService actual constructor() {
    private val secureRandom = SecureRandom()

    actual suspend fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(salt)

        val iv = ByteArray(IV_SIZE)
        secureRandom.nextBytes(iv)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))

        val encrypted = cipher.doFinal(data)

        // Combine: salt + iv + encrypted
        return salt + iv + encrypted
    }

    actual suspend fun decrypt(data: ByteArray, password: String): ByteArray {
        val salt = data.sliceArray(0 until SALT_SIZE)
        val iv = data.sliceArray(SALT_SIZE until SALT_SIZE + IV_SIZE)
        val encrypted = data.sliceArray(SALT_SIZE + IV_SIZE until data.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))

        return cipher.doFinal(encrypted)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
        private const val SALT_SIZE = 16
        private const val PBKDF2_ITERATIONS = 100_000
    }
}
