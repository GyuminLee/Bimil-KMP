package com.imbavchenko.bimil.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.imbavchenko.bimil.presentation.localization.strings

enum class PinSetupMode {
    SET_NEW,        // Setting new PIN
    CONFIRM,        // Confirming new PIN
    VERIFY_CURRENT, // Verifying current PIN before change/remove
    CHANGE          // Changing PIN (after verification)
}

@Composable
fun PinSetupDialog(
    isVisible: Boolean,
    isPinEnabled: Boolean,
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit,
    onPinRemove: () -> Unit,
    onVerifyCurrentPin: suspend (String) -> Boolean
) {
    if (!isVisible) return

    val strings = strings()
    var mode by remember { mutableStateOf(if (isPinEnabled) PinSetupMode.VERIFY_CURRENT else PinSetupMode.SET_NEW) }
    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var isRemoving by remember { mutableStateOf(false) }

    // Reset state when dialog opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            mode = if (isPinEnabled) PinSetupMode.VERIFY_CURRENT else PinSetupMode.SET_NEW
            pin = ""
            firstPin = ""
            errorMessage = null
            isRemoving = false
        }
    }

    val title = when (mode) {
        PinSetupMode.SET_NEW -> strings.setPin
        PinSetupMode.CONFIRM -> strings.confirmPin
        PinSetupMode.VERIFY_CURRENT -> if (isRemoving) strings.enterPin else strings.enterPin
        PinSetupMode.CHANGE -> strings.setPin
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PinInput(
                    pin = pin,
                    maxLength = 6,
                    onPinChange = { newPin ->
                        if (!isVerifying) {
                            pin = newPin
                            errorMessage = null
                        }
                    },
                    title = title,
                    errorMessage = errorMessage
                )

                // Handle PIN completion
                LaunchedEffect(pin) {
                    if (pin.length == 6 && !isVerifying) {
                        isVerifying = true
                        when (mode) {
                            PinSetupMode.SET_NEW -> {
                                if (pin.length < 4) {
                                    errorMessage = strings.pinTooShort
                                    pin = ""
                                } else {
                                    firstPin = pin
                                    pin = ""
                                    mode = PinSetupMode.CONFIRM
                                }
                            }
                            PinSetupMode.CONFIRM -> {
                                if (pin == firstPin) {
                                    onPinSet(pin)
                                    onDismiss()
                                } else {
                                    errorMessage = strings.pinMismatch
                                    pin = ""
                                    mode = PinSetupMode.SET_NEW
                                    firstPin = ""
                                }
                            }
                            PinSetupMode.VERIFY_CURRENT -> {
                                val isValid = onVerifyCurrentPin(pin)
                                if (isValid) {
                                    if (isRemoving) {
                                        onPinRemove()
                                        onDismiss()
                                    } else {
                                        pin = ""
                                        mode = PinSetupMode.CHANGE
                                    }
                                } else {
                                    errorMessage = strings.wrongPin
                                    pin = ""
                                }
                            }
                            PinSetupMode.CHANGE -> {
                                firstPin = pin
                                pin = ""
                                mode = PinSetupMode.CONFIRM
                            }
                        }
                        isVerifying = false
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                if (isPinEnabled && mode == PinSetupMode.VERIFY_CURRENT && !isRemoving) {
                    TextButton(
                        onClick = {
                            isRemoving = true
                            pin = ""
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = strings.removePin,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text(strings.cancel)
                }
            }
        }
    }
}
