package com.imbavchenko.bimil.domain.usecase

import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.getSettings()
}

class UpdateThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(theme: Theme) {
        settingsRepository.updateTheme(theme)
    }
}

class UpdateLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: String) {
        settingsRepository.updateLanguage(language)
    }
}

class UpdateRegionUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(region: Region) {
        settingsRepository.updateRegion(region)
    }
}

class SetPinUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(pin: String) {
        settingsRepository.setPin(pin)
    }
}

class VerifyPinUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(pin: String): Boolean {
        return settingsRepository.verifyPin(pin)
    }
}

class ClearPinUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.clearPin()
    }
}

class UpdateBiometricUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateBiometric(enabled)
    }
}

class UpdateAutoLockUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(seconds: Int) {
        settingsRepository.updateAutoLock(seconds)
    }
}

class InitializeSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.initializeSettings()
    }
}
