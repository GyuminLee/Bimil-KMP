package com.imbavchenko.bimil.data.encryption

actual class EncryptionService actual constructor() {

    actual suspend fun encrypt(data: ByteArray, password: String): ByteArray {
        // Placeholder - implement with CommonCrypto/CryptoKit
        return data
    }

    actual suspend fun decrypt(data: ByteArray, password: String): ByteArray {
        // Placeholder - implement with CommonCrypto/CryptoKit
        return data
    }
}
