package com.imbavchenko.bimil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.repository.RestoreResult
import com.imbavchenko.bimil.domain.usecase.ClearPinUseCase
import com.imbavchenko.bimil.domain.usecase.CreateBackupUseCase
import com.imbavchenko.bimil.domain.usecase.DeleteAllDataUseCase
import com.imbavchenko.bimil.domain.usecase.GetAccountCountUseCase
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.RestoreBackupUseCase
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
    val isLoading: Boolean = true,
    val backupInProgress: Boolean = false,
    val restoreInProgress: Boolean = false,
    val deleteInProgress: Boolean = false,
    val operationResult: OperationResult? = null
)

data class OperationResult(
    val success: Boolean,
    val message: String
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
    private val getAccountCountUseCase: GetAccountCountUseCase,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val deleteAllDataUseCase: DeleteAllDataUseCase
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

    suspend fun createBackup(password: String): ByteArray? {
        return try {
            _uiState.update { it.copy(backupInProgress = true) }
            val data = createBackupUseCase(password)
            _uiState.update { it.copy(backupInProgress = false) }
            data
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    backupInProgress = false,
                    operationResult = OperationResult(false, e.message ?: "Backup failed")
                )
            }
            null
        }
    }

    suspend fun restoreBackup(data: ByteArray, password: String): RestoreResult {
        return try {
            _uiState.update { it.copy(restoreInProgress = true) }
            val result = restoreBackupUseCase(data, password, merge = false)
            _uiState.update {
                it.copy(
                    restoreInProgress = false,
                    operationResult = if (result.success) {
                        OperationResult(true, "Restored ${result.accountsRestored} accounts")
                    } else {
                        OperationResult(false, result.error ?: "Restore failed")
                    }
                )
            }
            result
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    restoreInProgress = false,
                    operationResult = OperationResult(false, e.message ?: "Restore failed")
                )
            }
            RestoreResult(false, 0, 0, e.message)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(deleteInProgress = true) }
                deleteAllDataUseCase()
                _uiState.update {
                    it.copy(
                        deleteInProgress = false,
                        operationResult = OperationResult(true, "All data deleted")
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        deleteInProgress = false,
                        operationResult = OperationResult(false, e.message ?: "Delete failed")
                    )
                }
            }
        }
    }

    fun clearOperationResult() {
        _uiState.update { it.copy(operationResult = null) }
    }
}
