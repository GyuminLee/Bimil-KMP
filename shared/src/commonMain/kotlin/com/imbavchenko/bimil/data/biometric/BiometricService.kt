package com.imbavchenko.bimil.data.biometric

interface BiometricService {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
}
