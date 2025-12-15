package com.imbavchenko.bimil.platform

import com.imbavchenko.bimil.data.biometric.BiometricService

class DesktopBiometricService : BiometricService {
    override fun isBiometricAvailable(): Boolean {
        // Desktop doesn't support biometric authentication in most cases
        // Could potentially integrate with Windows Hello or macOS Touch ID in the future
        return false
    }

    override suspend fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        // Desktop biometric not supported
        onError("Biometric authentication is not supported on this platform")
    }
}
