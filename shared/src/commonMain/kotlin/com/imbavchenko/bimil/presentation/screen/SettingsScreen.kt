package com.imbavchenko.bimil.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.presentation.component.PinSetupDialog
import com.imbavchenko.bimil.presentation.localization.Language
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onCreateBackup: (ByteArray, String) -> Unit = { _, _ -> }, // data, fileName
    onSelectBackupFile: () -> Unit = {},
    pendingRestoreData: ByteArray? = null,
    onRestoreComplete: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    biometricService: BiometricService = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = strings()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinDialog by remember { mutableStateOf(false) }
    var showPinSetupForEnable by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    // Don't use remember - check every recomposition to handle Activity lifecycle
    val isBiometricAvailable = biometricService.isBiometricAvailable()

    // Handle pending restore data
    LaunchedEffect(pendingRestoreData) {
        if (pendingRestoreData != null) {
            showRestorePasswordDialog = true
        }
    }

    // Show operation results
    LaunchedEffect(uiState.operationResult) {
        uiState.operationResult?.let { result ->
            snackbarHostState.showSnackbar(result.message)
            viewModel.clearOperationResult()
        }
    }

    // PIN Setup Dialog for changing PIN
    PinSetupDialog(
        isVisible = showPinDialog,
        isPinEnabled = uiState.settings.isPinEnabled,
        onDismiss = { showPinDialog = false },
        onPinSet = { pin ->
            viewModel.setPin(pin)
        },
        onPinRemove = {
            viewModel.clearPin()
            // Also disable biometric when PIN is removed
            if (uiState.settings.isBiometricEnabled) {
                viewModel.updateBiometric(false)
            }
        },
        onVerifyCurrentPin = { pin ->
            viewModel.verifyPin(pin)
        }
    )

    // PIN Setup Dialog for enabling PIN lock
    PinSetupDialog(
        isVisible = showPinSetupForEnable,
        isPinEnabled = false, // Always show as new PIN setup
        onDismiss = { showPinSetupForEnable = false },
        onPinSet = { pin ->
            viewModel.setPin(pin)
            showPinSetupForEnable = false
        },
        onPinRemove = {},
        onVerifyCurrentPin = { false }
    )

    // Backup Password Dialog
    if (showBackupPasswordDialog) {
        PasswordInputDialog(
            title = strings.createBackup,
            message = strings.enterBackupPassword,
            onDismiss = { showBackupPasswordDialog = false },
            onConfirm = { password ->
                showBackupPasswordDialog = false
                scope.launch {
                    val data = viewModel.createBackup(password)
                    if (data != null) {
                        val fileName = "bimil_backup_${System.currentTimeMillis()}.bak"
                        onCreateBackup(data, fileName)
                    }
                }
            }
        )
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && pendingRestoreData != null) {
        PasswordInputDialog(
            title = strings.restoreBackup,
            message = strings.enterBackupPassword,
            requireConfirmation = false,
            onDismiss = {
                showRestorePasswordDialog = false
                onRestoreComplete()
            },
            onConfirm = { password ->
                showRestorePasswordDialog = false
                scope.launch {
                    viewModel.restoreBackup(pendingRestoreData, password)
                    onRestoreComplete()
                }
            }
        )
    }

    // Delete All Data Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(strings.deleteAllData) },
            text = { Text(strings.deleteAllDataConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAllData()
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.settings,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Section
            SettingsSection(title = strings.security) {
                // PIN Lock Switch
                SettingsRow(
                    title = strings.pinLock,
                    subtitle = strings.pinLockSubtitle,
                    trailing = {
                        Switch(
                            checked = uiState.settings.isPinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // Show PIN setup dialog
                                    showPinSetupForEnable = true
                                } else {
                                    // Disable PIN and biometric
                                    viewModel.clearPin()
                                    if (uiState.settings.isBiometricEnabled) {
                                        viewModel.updateBiometric(false)
                                    }
                                }
                            }
                        )
                    }
                )

                // Change PIN (only visible when PIN is enabled)
                if (uiState.settings.isPinEnabled) {
                    SettingsRow(
                        title = strings.changePin,
                        subtitle = strings.changePinSubtitle,
                        onClick = { showPinDialog = true }
                    )
                }

                // Biometric Unlock Switch
                SettingsRow(
                    title = strings.biometricUnlock,
                    subtitle = if (!isBiometricAvailable) strings.biometricNotAvailable else strings.biometricSubtitle,
                    trailing = {
                        Switch(
                            checked = uiState.settings.isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateBiometric(enabled)
                            },
                            enabled = uiState.settings.isPinEnabled && isBiometricAvailable
                        )
                    }
                )
            }

            // General Section
            SettingsSection(title = strings.general) {
                SettingsDropdownRow(
                    title = strings.theme,
                    selected = when (uiState.settings.theme) {
                        Theme.LIGHT -> strings.light
                        Theme.DARK -> strings.dark
                        Theme.SYSTEM -> strings.system
                    },
                    options = listOf(strings.light, strings.dark, strings.system),
                    onSelect = { option ->
                        val theme = when (option) {
                            strings.light -> Theme.LIGHT
                            strings.dark -> Theme.DARK
                            else -> Theme.SYSTEM
                        }
                        viewModel.updateTheme(theme)
                    }
                )

                SettingsDropdownRow(
                    title = strings.language,
                    selected = when (uiState.settings.language) {
                        "SYSTEM" -> strings.system
                        "ko" -> Language.KOREAN.nativeName
                        "en" -> Language.ENGLISH.nativeName
                        "ja" -> Language.JAPANESE.nativeName
                        "zh" -> Language.CHINESE.nativeName
                        "de" -> Language.GERMAN.nativeName
                        else -> uiState.settings.language
                    },
                    options = listOf(
                        strings.system,
                        Language.ENGLISH.nativeName,
                        Language.KOREAN.nativeName,
                        Language.JAPANESE.nativeName,
                        Language.CHINESE.nativeName,
                        Language.GERMAN.nativeName
                    ),
                    onSelect = { option ->
                        val language = when (option) {
                            strings.system -> "SYSTEM"
                            Language.ENGLISH.nativeName -> "en"
                            Language.KOREAN.nativeName -> "ko"
                            Language.JAPANESE.nativeName -> "ja"
                            Language.CHINESE.nativeName -> "zh"
                            Language.GERMAN.nativeName -> "de"
                            else -> "SYSTEM"
                        }
                        viewModel.updateLanguage(language)
                    }
                )

                SettingsDropdownRow(
                    title = strings.region,
                    selected = uiState.settings.region.displayName,
                    options = Region.entries.map { it.displayName },
                    onSelect = { option ->
                        val region = Region.entries.find { it.displayName == option } ?: Region.GLOBAL
                        viewModel.updateRegion(region)
                    }
                )
            }

            // Data Section
            SettingsSection(title = strings.data) {
                SettingsRow(
                    title = strings.savedItems,
                    subtitle = "${uiState.accountCount} ${strings.items}"
                )

                SettingsRow(
                    title = strings.createBackup,
                    subtitle = strings.createBackupSubtitle,
                    onClick = { showBackupPasswordDialog = true },
                    enabled = !uiState.backupInProgress
                )

                SettingsRow(
                    title = strings.restoreBackup,
                    subtitle = strings.restoreBackupSubtitle,
                    onClick = onSelectBackupFile,
                    enabled = !uiState.restoreInProgress
                )

                SettingsRow(
                    title = strings.deleteAllData,
                    subtitle = strings.deleteAllDataSubtitle,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteConfirmDialog = true },
                    enabled = !uiState.deleteInProgress
                )
            }

            // About Section
            SettingsSection(title = strings.about) {
                SettingsRow(
                    title = strings.version,
                    subtitle = "1.1.0"
                )

                SettingsRow(
                    title = strings.privacyPolicy,
                    onClick = { /* TODO: Open privacy policy */ }
                )

                SettingsRow(
                    title = strings.openSourceLicenses,
                    onClick = { /* TODO: Open licenses */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val actualTitleColor = if (enabled) titleColor else titleColor.copy(alpha = 0.5f)
    val actualSubtitleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = actualTitleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = actualSubtitleColor
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsDropdownRow(
    title: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(option)
                                if (option == selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordInputDialog(
    title: String,
    message: String,
    requireConfirmation: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val strings = strings()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message)
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text(strings.password) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (requireConfirmation) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            showError = false
                        },
                        label = { Text(strings.confirmPassword) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = showError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showError) {
                        Text(
                            text = strings.passwordMismatch,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (requireConfirmation) {
                        if (password == confirmPassword && password.isNotEmpty()) {
                            onConfirm(password)
                        } else {
                            showError = true
                        }
                    } else {
                        if (password.isNotEmpty()) {
                            onConfirm(password)
                        }
                    }
                },
                enabled = if (requireConfirmation) {
                    password.isNotEmpty() && confirmPassword.isNotEmpty()
                } else {
                    password.isNotEmpty()
                }
            ) {
                Text(strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
