package com.imbavchenko.bimil.data.encryption

expect class EncryptionService() {
    suspend fun encrypt(data: ByteArray, password: String): ByteArray
    suspend fun decrypt(data: ByteArray, password: String): ByteArray
}
