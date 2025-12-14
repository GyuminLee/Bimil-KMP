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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
            SettingsSection(title = "Security") {
                SettingsRow(
                    title = "PIN Lock",
                    subtitle = if (uiState.settings.isPinEnabled) "Enabled" else "Disabled",
                    onClick = { /* TODO: Open PIN setup dialog */ }
                )

                SettingsRow(
                    title = "Biometric Unlock",
                    subtitle = "Use fingerprint or face",
                    trailing = {
                        Switch(
                            checked = uiState.settings.isBiometricEnabled,
                            onCheckedChange = { viewModel.updateBiometric(it) },
                            enabled = uiState.settings.isPinEnabled
                        )
                    }
                )

                SettingsDropdownRow(
                    title = "Auto-lock",
                    selected = when (uiState.settings.autoLockSeconds) {
                        -1 -> "Never"
                        0 -> "Immediately"
                        30 -> "30 seconds"
                        60 -> "1 minute"
                        300 -> "5 minutes"
                        else -> "${uiState.settings.autoLockSeconds}s"
                    },
                    options = listOf("Never", "Immediately", "30 seconds", "1 minute", "5 minutes"),
                    onSelect = { option ->
                        val seconds = when (option) {
                            "Never" -> -1
                            "Immediately" -> 0
                            "30 seconds" -> 30
                            "1 minute" -> 60
                            "5 minutes" -> 300
                            else -> -1
                        }
                        viewModel.updateAutoLock(seconds)
                    }
                )
            }

            // General Section
            SettingsSection(title = "General") {
                SettingsDropdownRow(
                    title = "Theme",
                    selected = when (uiState.settings.theme) {
                        Theme.LIGHT -> "Light"
                        Theme.DARK -> "Dark"
                        Theme.SYSTEM -> "System"
                    },
                    options = listOf("Light", "Dark", "System"),
                    onSelect = { option ->
                        val theme = when (option) {
                            "Light" -> Theme.LIGHT
                            "Dark" -> Theme.DARK
                            else -> Theme.SYSTEM
                        }
                        viewModel.updateTheme(theme)
                    }
                )

                SettingsDropdownRow(
                    title = "Language",
                    selected = when (uiState.settings.language) {
                        "SYSTEM" -> "System"
                        "ko" -> "Korean"
                        "en" -> "English"
                        "ja" -> "Japanese"
                        "zh" -> "Chinese"
                        else -> uiState.settings.language
                    },
                    options = listOf("System", "English", "Korean", "Japanese", "Chinese"),
                    onSelect = { option ->
                        val language = when (option) {
                            "System" -> "SYSTEM"
                            "English" -> "en"
                            "Korean" -> "ko"
                            "Japanese" -> "ja"
                            "Chinese" -> "zh"
                            else -> "SYSTEM"
                        }
                        viewModel.updateLanguage(language)
                    }
                )

                SettingsDropdownRow(
                    title = "Region",
                    selected = uiState.settings.region.displayName,
                    options = Region.entries.map { it.displayName },
                    onSelect = { option ->
                        val region = Region.entries.find { it.displayName == option } ?: Region.GLOBAL
                        viewModel.updateRegion(region)
                    }
                )
            }

            // Data Section
            SettingsSection(title = "Data") {
                SettingsRow(
                    title = "Saved Items",
                    subtitle = "${uiState.accountCount} items"
                )

                SettingsRow(
                    title = "Create Backup",
                    subtitle = "Export encrypted backup file",
                    onClick = onNavigateToBackup
                )

                SettingsRow(
                    title = "Restore Backup",
                    subtitle = "Import from backup file",
                    onClick = onNavigateToBackup
                )

                SettingsRow(
                    title = "Delete All Data",
                    subtitle = "This cannot be undone",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { /* TODO: Show confirmation dialog */ }
                )
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsRow(
                    title = "Version",
                    subtitle = "1.1.0"
                )

                SettingsRow(
                    title = "Privacy Policy",
                    onClick = { /* TODO: Open privacy policy */ }
                )

                SettingsRow(
                    title = "Open Source Licenses",
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
