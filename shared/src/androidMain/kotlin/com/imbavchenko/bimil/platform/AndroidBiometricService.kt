package com.imbavchenko.bimil.platform

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.di.ActivityProvider

class AndroidBiometricService(
    private val context: Context,
    private val activityProvider: ActivityProvider
) : BiometricService {

    private fun getAvailableAuthenticator(): Int? {
        return try {
            val biometricManager = BiometricManager.from(context)

            // Try each authenticator type and return the first one that works
            val authenticatorsToTry = listOf(
                BiometricManager.Authenticators.BIOMETRIC_STRONG,
                BiometricManager.Authenticators.BIOMETRIC_WEAK,
                // For Android 11+, try combined check
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                } else null
            ).filterNotNull()

            for (authenticator in authenticatorsToTry) {
                if (biometricManager.canAuthenticate(authenticator) == BiometricManager.BIOMETRIC_SUCCESS) {
                    // For DEVICE_CREDENTIAL combo, only return BIOMETRIC_STRONG
                    return if (authenticator and BiometricManager.Authenticators.DEVICE_CREDENTIAL != 0) {
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    } else {
                        authenticator
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun isBiometricAvailable(): Boolean {
        return getAvailableAuthenticator() != null
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

        val authenticator = getAvailableAuthenticator() ?: run {
            onError("Biometric not available")
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

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticator)

        // Only set negative button if NOT using DEVICE_CREDENTIAL
        // (DEVICE_CREDENTIAL provides its own cancel mechanism)
        if (authenticator and BiometricManager.Authenticators.DEVICE_CREDENTIAL == 0) {
            promptInfoBuilder.setNegativeButtonText("Cancel")
        }

        try {
            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError(e.message ?: "Authentication failed")
        }
    }
}
