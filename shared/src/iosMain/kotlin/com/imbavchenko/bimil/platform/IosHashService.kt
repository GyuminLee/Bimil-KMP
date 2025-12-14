package com.imbavchenko.bimil.data.encryption

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.Foundation.base64EncodedStringWithOptions
import platform.posix.memcpy
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo

actual class HashService actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        saltBytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, 16u, pinned.addressOf(0))
        }
        return saltBytes.toNSData().base64EncodedStringWithOptions(0u)
    }

    actual fun hashPin(pin: String, salt: String): String {
        // Simple SHA-256 implementation for iOS
        // In production, use CommonCrypto
        val saltBytes = salt.decodeBase64()
        val combined = saltBytes + pin.encodeToByteArray()
        val hash = sha256(combined)
        return hash.toNSData().base64EncodedStringWithOptions(0u)
    }

    private fun sha256(data: ByteArray): ByteArray {
        // Simplified - in production use CC_SHA256
        return data // Placeholder - implement with CommonCrypto
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData = memScoped {
        NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
    }

    private fun String.decodeBase64(): ByteArray {
        // Placeholder - implement base64 decode
        return ByteArray(0)
    }
}
