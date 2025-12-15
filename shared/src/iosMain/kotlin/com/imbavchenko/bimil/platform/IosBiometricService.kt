package com.imbavchenko.bimil.platform

import com.imbavchenko.bimil.data.biometric.BiometricService
import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Foundation.NSError

class IosBiometricService : BiometricService {
    @OptIn(ExperimentalForeignApi::class)
    override fun isBiometricAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val context = LAContext()
        context.localizedCancelTitle = "Cancel"

        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = subtitle
        ) { success, error ->
            if (success) {
                onSuccess()
            } else {
                val nsError = error as? NSError
                when (nsError?.code) {
                    -2L, -4L, -9L -> onCancel() // User cancel, system cancel, app cancel
                    else -> onError(nsError?.localizedDescription ?: "Authentication failed")
                }
            }
        }
    }
}
