package com.imbavchenko.bimil.platform

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.di.ActivityProvider

class AndroidBiometricService(
    private val context: Context,
    private val activityProvider: ActivityProvider
) : BiometricService {
    override fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        // Check for any biometric - try WEAK first (more permissive, includes fingerprint/face)
        val canAuthenticateWeak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val canAuthenticateStrong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        return canAuthenticateWeak == BiometricManager.BIOMETRIC_SUCCESS ||
               canAuthenticateStrong == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val activity = activityProvider.getActivity() ?: run {
            onError("Activity not available")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED) {
                    onCancel()
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onError here - this is just a failed attempt, user can try again
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        // Use the same authenticator type that's available
        val biometricManager = BiometricManager.from(context)
        val authenticators = when {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            else ->
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
