package com.imbavchenko.bimil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.usecase.ClearPinUseCase
import com.imbavchenko.bimil.domain.usecase.GetAccountCountUseCase
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.SetPinUseCase
import com.imbavchenko.bimil.domain.usecase.VerifyPinUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateAutoLockUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateBiometricUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateLanguageUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateRegionUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val accountCount: Long = 0,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateRegionUseCase: UpdateRegionUseCase,
    private val setPinUseCase: SetPinUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    private val clearPinUseCase: ClearPinUseCase,
    private val updateBiometricUseCase: UpdateBiometricUseCase,
    private val updateAutoLockUseCase: UpdateAutoLockUseCase,
    private val getAccountCountUseCase: GetAccountCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadAccountCount()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    private fun loadAccountCount() {
        viewModelScope.launch {
            getAccountCountUseCase().collectLatest { count ->
                _uiState.update { it.copy(accountCount = count) }
            }
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            updateThemeUseCase(theme)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
        }
    }

    fun updateRegion(region: Region) {
        viewModelScope.launch {
            updateRegionUseCase(region)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            setPinUseCase(pin)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        return verifyPinUseCase(pin)
    }

    fun clearPin() {
        viewModelScope.launch {
            clearPinUseCase()
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            updateBiometricUseCase(enabled)
        }
    }

    fun updateAutoLock(seconds: Int) {
        viewModelScope.launch {
            updateAutoLockUseCase(seconds)
        }
    }
}
