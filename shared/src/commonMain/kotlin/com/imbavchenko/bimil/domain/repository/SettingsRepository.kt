package com.imbavchenko.bimil.domain.repository

import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>

    suspend fun initializeSettings()
    suspend fun updateTheme(theme: Theme)
    suspend fun updateLanguage(language: String)
    suspend fun updateRegion(region: Region)
    suspend fun updatePin(pinHash: String?, pinSalt: String?)
    suspend fun clearPin()
    suspend fun updateBiometric(enabled: Boolean)
    suspend fun updateAutoLock(seconds: Int)

    suspend fun verifyPin(pin: String): Boolean
    suspend fun setPin(pin: String)
}
