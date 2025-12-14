package com.imbavchenko.bimil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbavchenko.bimil.domain.model.AccountEntry
import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.RequirementStatus
import com.imbavchenko.bimil.domain.model.SsoHint
import com.imbavchenko.bimil.domain.model.SsoProvider
import com.imbavchenko.bimil.domain.usecase.GetAccountWithHintUseCase
import com.imbavchenko.bimil.domain.usecase.GetAllCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.SaveAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AddEditUiState(
    val isEditing: Boolean = false,
    val accountId: String = "",

    // Basic info
    val serviceName: String = "",
    val username: String = "",
    val websiteUrl: String = "",
    val memo: String = "",
    val categoryId: String = "other",
    val isFavorite: Boolean = false,

    // Login type
    val loginType: LoginType = LoginType.PASSWORD,

    // SSO
    val ssoProvider: SsoProvider = SsoProvider.GOOGLE,
    val ssoProviderCustom: String = "",
    val availableSsoProviders: List<SsoProvider> = emptyList(),

    // Password hints
    val minLength: Int = 8,
    val maxLength: Int? = null,
    val requiresSpecial: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresUppercase: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresLowercase: RequirementStatus = RequirementStatus.UNKNOWN,
    val requiresNumber: RequirementStatus = RequirementStatus.UNKNOWN,
    val allowedSpecialChars: String = "",
    val personalHint: String = "",

    // Categories
    val categories: List<Category> = emptyList(),

    // UI state
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = serviceName.isNotBlank() && username.isNotBlank()
}

class AddEditAccountViewModel(
    private val getAccountWithHintUseCase: GetAccountWithHintUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveAccountUseCase: SaveAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadRegion()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getAllCategoriesUseCase().collectLatest { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadRegion() {
        viewModelScope.launch {
            val settings = getSettingsUseCase().first()
            val providers = SsoProvider.getByRegion(settings.region)
            _uiState.update { it.copy(availableSsoProviders = providers) }
        }
    }

    fun loadAccount(accountId: String?) {
        if (accountId == null) {
            _uiState.update {
                it.copy(
                    isEditing = false,
                    accountId = generateId()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditing = true) }

            getAccountWithHintUseCase(accountId).first()?.let { accountWithHint ->
                val account = accountWithHint.account
                val ssoHint = accountWithHint.ssoHint
                val passwordHint = accountWithHint.passwordHint

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accountId = account.id,
                        serviceName = account.serviceName,
                        username = account.username,
                        websiteUrl = account.websiteUrl ?: "",
                        memo = account.memo ?: "",
                        categoryId = account.categoryId,
                        isFavorite = account.isFavorite,
                        loginType = account.loginType,
                        ssoProvider = ssoHint?.provider ?: SsoProvider.GOOGLE,
                        ssoProviderCustom = ssoHint?.providerCustom ?: "",
                        minLength = passwordHint?.minLength ?: 8,
                        maxLength = passwordHint?.maxLength,
                        requiresSpecial = passwordHint?.requiresSpecial ?: RequirementStatus.UNKNOWN,
                        requiresUppercase = passwordHint?.requiresUppercase ?: RequirementStatus.UNKNOWN,
                        requiresLowercase = passwordHint?.requiresLowercase ?: RequirementStatus.UNKNOWN,
                        requiresNumber = passwordHint?.requiresNumber ?: RequirementStatus.UNKNOWN,
                        allowedSpecialChars = passwordHint?.allowedSpecialChars ?: "",
                        personalHint = passwordHint?.personalHint ?: ""
                    )
                }
            }
        }
    }

    fun updateServiceName(value: String) {
        _uiState.update { it.copy(serviceName = value) }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun updateWebsiteUrl(value: String) {
        _uiState.update { it.copy(websiteUrl = value) }
    }

    fun updateMemo(value: String) {
        _uiState.update { it.copy(memo = value) }
    }

    fun updateCategory(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun updateLoginType(loginType: LoginType) {
        _uiState.update { it.copy(loginType = loginType) }
    }

    fun updateSsoProvider(provider: SsoProvider) {
        _uiState.update { it.copy(ssoProvider = provider) }
    }

    fun updateSsoProviderCustom(value: String) {
        _uiState.update { it.copy(ssoProviderCustom = value) }
    }

    fun updateMinLength(value: Int) {
        _uiState.update { it.copy(minLength = value.coerceIn(1, 128)) }
    }

    fun updateMaxLength(value: Int?) {
        _uiState.update { it.copy(maxLength = value?.coerceIn(1, 128)) }
    }

    fun updateRequiresSpecial(value: RequirementStatus) {
        _uiState.update { it.copy(requiresSpecial = value) }
    }

    fun updateRequiresUppercase(value: RequirementStatus) {
        _uiState.update { it.copy(requiresUppercase = value) }
    }

    fun updateRequiresLowercase(value: RequirementStatus) {
        _uiState.update { it.copy(requiresLowercase = value) }
    }

    fun updateRequiresNumber(value: RequirementStatus) {
        _uiState.update { it.copy(requiresNumber = value) }
    }

    fun updateAllowedSpecialChars(value: String) {
        _uiState.update { it.copy(allowedSpecialChars = value) }
    }

    fun updatePersonalHint(value: String) {
        _uiState.update { it.copy(personalHint = value.take(200)) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val now = Clock.System.now().toEpochMilliseconds()

                val account = AccountEntry(
                    id = state.accountId,
                    serviceName = state.serviceName.trim(),
                    username = state.username.trim(),
                    websiteUrl = state.websiteUrl.takeIf { it.isNotBlank() },
                    loginType = state.loginType,
                    categoryId = state.categoryId,
                    isFavorite = state.isFavorite,
                    memo = state.memo.takeIf { it.isNotBlank() },
                    iconData = null,
                    viewCount = 0,
                    createdAt = now,
                    updatedAt = now
                )

                val ssoHint = if (state.loginType == LoginType.SSO) {
                    SsoHint(
                        id = generateId(),
                        accountId = state.accountId,
                        provider = state.ssoProvider,
                        providerCustom = if (state.ssoProvider == SsoProvider.CUSTOM) state.ssoProviderCustom else null
                    )
                } else null

                val passwordHint = if (state.loginType == LoginType.PASSWORD) {
                    PasswordHint(
                        id = generateId(),
                        accountId = state.accountId,
                        minLength = state.minLength,
                        maxLength = state.maxLength,
                        requiresSpecial = state.requiresSpecial,
                        requiresUppercase = state.requiresUppercase,
                        requiresLowercase = state.requiresLowercase,
                        requiresNumber = state.requiresNumber,
                        allowedSpecialChars = state.allowedSpecialChars.takeIf { it.isNotBlank() },
                        personalHint = state.personalHint.takeIf { it.isNotBlank() }
                    )
                } else null

                saveAccountUseCase(account, ssoHint, passwordHint)

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateId(): String {
        return Uuid.random().toString()
    }
}
