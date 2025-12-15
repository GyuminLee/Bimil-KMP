package com.imbavchenko.bimil.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imbavchenko.bimil.presentation.component.PinInput
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.theme.BimilColors
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    onVerifyPin: suspend (String) -> Boolean,
    onBiometricClick: () -> Unit,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean
) {
    val strings = strings()
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    // Auto-verify when PIN reaches 6 digits
    LaunchedEffect(pin) {
        if (pin.length == 6 && !isVerifying) {
            isVerifying = true
            errorMessage = null

            val isValid = onVerifyPin(pin)
            if (isValid) {
                onUnlock()
            } else {
                errorMessage = strings.wrongPin
                delay(300)
                pin = ""
            }
            isVerifying = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App logo/icon area
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = BimilColors.Primary
                )

                Spacer(modifier = Modifier.height(48.dp))

                PinInput(
                    pin = pin,
                    maxLength = 6,
                    onPinChange = { newPin ->
                        if (!isVerifying) {
                            pin = newPin
                            errorMessage = null
                        }
                    },
                    title = strings.enterPin,
                    errorMessage = errorMessage
                )

                // Biometric button
                if (isBiometricAvailable && isBiometricEnabled) {
                    Spacer(modifier = Modifier.height(32.dp))

                    IconButton(
                        onClick = onBiometricClick,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = strings.useBiometric,
                            modifier = Modifier.size(48.dp),
                            tint = BimilColors.Primary
                        )
                    }

                    Text(
                        text = strings.useBiometric,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
