package com.imbavchenko.bimil.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.RequirementStatus
import com.imbavchenko.bimil.presentation.viewmodel.AddEditAccountViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    accountId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Account" else "Add Account",
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
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.isValid && !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (uiState.isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
            // Basic Info Section
            SectionCard(title = "Basic Information") {
                OutlinedTextField(
                    value = uiState.serviceName,
                    onValueChange = viewModel::updateServiceName,
                    label = { Text("Service Name *") },
                    placeholder = { Text("e.g., Google, Netflix") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text("Username / Email *") },
                    placeholder = { Text("e.g., user@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.websiteUrl,
                    onValueChange = viewModel::updateWebsiteUrl,
                    label = { Text("Website URL (optional)") },
                    placeholder = { Text("e.g., https://example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown
                CategoryDropdown(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.categoryId,
                    onCategorySelected = viewModel::updateCategory
                )
            }

            // Login Type Section
            SectionCard(title = "Login Type") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = uiState.loginType == LoginType.PASSWORD,
                        onClick = { viewModel.updateLoginType(LoginType.PASSWORD) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Password")
                    }
                    SegmentedButton(
                        selected = uiState.loginType == LoginType.SSO,
                        onClick = { viewModel.updateLoginType(LoginType.SSO) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("SSO")
                    }
                }
            }

            // SSO or Password hints based on selection
            if (uiState.loginType == LoginType.SSO) {
                SsoSection(
                    providers = uiState.availableSsoProviders,
                    selectedProvider = uiState.ssoProvider,
                    customProvider = uiState.ssoProviderCustom,
                    onProviderSelected = viewModel::updateSsoProvider,
                    onCustomProviderChange = viewModel::updateSsoProviderCustom
                )
            } else {
                PasswordHintSection(
                    minLength = uiState.minLength,
                    requiresSpecial = uiState.requiresSpecial,
                    requiresUppercase = uiState.requiresUppercase,
                    requiresLowercase = uiState.requiresLowercase,
                    requiresNumber = uiState.requiresNumber,
                    personalHint = uiState.personalHint,
                    onMinLengthChange = viewModel::updateMinLength,
                    onRequiresSpecialChange = viewModel::updateRequiresSpecial,
                    onRequiresUppercaseChange = viewModel::updateRequiresUppercase,
                    onRequiresLowercaseChange = viewModel::updateRequiresLowercase,
                    onRequiresNumberChange = viewModel::updateRequiresNumber,
                    onPersonalHintChange = viewModel::updatePersonalHint
                )
            }

            // Memo
            SectionCard(title = "Memo (optional)") {
                OutlinedTextField(
                    value = uiState.memo,
                    onValueChange = viewModel::updateMemo,
                    placeholder = { Text("Add any additional notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<com.imbavchenko.bimil.domain.model.Category>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Select category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SsoSection(
    providers: List<com.imbavchenko.bimil.domain.model.SsoProvider>,
    selectedProvider: com.imbavchenko.bimil.domain.model.SsoProvider,
    customProvider: String,
    onProviderSelected: (com.imbavchenko.bimil.domain.model.SsoProvider) -> Unit,
    onCustomProviderChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SectionCard(title = "SSO Provider") {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedProvider.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Provider") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                providers.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider.displayName) },
                        onClick = {
                            onProviderSelected(provider)
                            expanded = false
                        }
                    )
                }
                // Add Custom option
                DropdownMenuItem(
                    text = { Text("Other...") },
                    onClick = {
                        onProviderSelected(com.imbavchenko.bimil.domain.model.SsoProvider.CUSTOM)
                        expanded = false
                    }
                )
            }
        }

        if (selectedProvider == com.imbavchenko.bimil.domain.model.SsoProvider.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customProvider,
                onValueChange = onCustomProviderChange,
                label = { Text("Custom Provider Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun PasswordHintSection(
    minLength: Int,
    requiresSpecial: RequirementStatus,
    requiresUppercase: RequirementStatus,
    requiresLowercase: RequirementStatus,
    requiresNumber: RequirementStatus,
    personalHint: String,
    onMinLengthChange: (Int) -> Unit,
    onRequiresSpecialChange: (RequirementStatus) -> Unit,
    onRequiresUppercaseChange: (RequirementStatus) -> Unit,
    onRequiresLowercaseChange: (RequirementStatus) -> Unit,
    onRequiresNumberChange: (RequirementStatus) -> Unit,
    onPersonalHintChange: (String) -> Unit
) {
    SectionCard(title = "Password Requirements") {
        OutlinedTextField(
            value = minLength.toString(),
            onValueChange = { it.toIntOrNull()?.let(onMinLengthChange) },
            label = { Text("Minimum Length") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        RequirementRow("Special characters (!@#)", requiresSpecial, onRequiresSpecialChange)
        RequirementRow("Uppercase letters (A-Z)", requiresUppercase, onRequiresUppercaseChange)
        RequirementRow("Lowercase letters (a-z)", requiresLowercase, onRequiresLowercaseChange)
        RequirementRow("Numbers (0-9)", requiresNumber, onRequiresNumberChange)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = personalHint,
            onValueChange = onPersonalHintChange,
            label = { Text("Personal Hint") },
            placeholder = { Text("e.g., birthday + first car name") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            supportingText = { Text("${personalHint.length}/200") }
        )
    }
}

@Composable
private fun RequirementRow(
    label: String,
    status: RequirementStatus,
    onStatusChange: (RequirementStatus) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RequirementStatus.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = status == option,
                            onClick = { onStatusChange(option) },
                            role = Role.RadioButton
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = status == option,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (option) {
                            RequirementStatus.YES -> "Yes"
                            RequirementStatus.NO -> "No"
                            RequirementStatus.UNKNOWN -> "?"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
