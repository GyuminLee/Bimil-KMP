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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToBackup: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    biometricService: BiometricService = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = strings()
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    val isBiometricAvailable = remember { biometricService.isBiometricAvailable() }

    // PIN Setup Dialog
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

    Scaffold(
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
                SettingsRow(
                    title = strings.pinLock,
                    subtitle = if (uiState.settings.isPinEnabled) strings.enabled else strings.disabled,
                    onClick = { showPinDialog = true }
                )

                SettingsRow(
                    title = strings.biometricUnlock,
                    subtitle = if (!isBiometricAvailable) strings.biometricNotAvailable else strings.biometricSubtitle,
                    trailing = {
                        Switch(
                            checked = uiState.settings.isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !uiState.settings.isPinEnabled) {
                                    // Show PIN setup first
                                    showPinDialog = true
                                } else {
                                    viewModel.updateBiometric(enabled)
                                }
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
                    onClick = onNavigateToBackup
                )

                SettingsRow(
                    title = strings.restoreBackup,
                    subtitle = strings.restoreBackupSubtitle,
                    onClick = onNavigateToBackup
                )

                SettingsRow(
                    title = strings.deleteAllData,
                    subtitle = strings.deleteAllDataSubtitle,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { /* TODO: Show confirmation dialog */ }
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
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
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
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
