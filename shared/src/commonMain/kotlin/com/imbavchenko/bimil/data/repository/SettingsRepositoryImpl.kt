package com.imbavchenko.bimil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.imbavchenko.bimil.data.encryption.HashService
import com.imbavchenko.bimil.db.BimilDatabase
import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Region
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SettingsRepositoryImpl(
    private val database: BimilDatabase,
    private val hashService: HashService
) : SettingsRepository {

    private val queries = database.appSettingsQueries

    override fun getSettings(): Flow<AppSettings> {
        return queries.getSettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { settings ->
                settings?.toDomainModel() ?: AppSettings()
            }
    }

    override suspend fun initializeSettings() = withContext(Dispatchers.IO) {
        val existingSettings = queries.getSettings().executeAsOneOrNull()
        if (existingSettings == null) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insertSettings(
                theme = Theme.SYSTEM.name,
                language = "SYSTEM",
                region = Region.GLOBAL.name,
                pin_hash = null,
                pin_salt = null,
                biometric_enabled = 0L,
                auto_lock_seconds = -1L,
                schema_version = 1L,
                created_at = now,
                updated_at = now
            )
        }
    }

    override suspend fun updateTheme(theme: Theme) = withContext(Dispatchers.IO) {
        queries.updateTheme(theme.name, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updateLanguage(language: String) = withContext(Dispatchers.IO) {
        queries.updateLanguage(language, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updateRegion(region: Region) = withContext(Dispatchers.IO) {
        queries.updateRegion(region.name, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updatePin(pinHash: String?, pinSalt: String?) = withContext(Dispatchers.IO) {
        queries.updatePin(pinHash, pinSalt, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun clearPin() = withContext(Dispatchers.IO) {
        queries.clearPin(Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updateBiometric(enabled: Boolean) = withContext(Dispatchers.IO) {
        queries.updateBiometric(if (enabled) 1L else 0L, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun updateAutoLock(seconds: Int) = withContext(Dispatchers.IO) {
        queries.updateAutoLock(seconds.toLong(), Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun setPin(pin: String) = withContext(Dispatchers.IO) {
        val salt = hashService.generateSalt()
        val hash = hashService.hashPin(pin, salt)
        updatePin(hash, salt)
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val settings = queries.getSettings().executeAsOneOrNull() ?: return@withContext false
        val storedHash = settings.pin_hash ?: return@withContext false
        val storedSalt = settings.pin_salt ?: return@withContext false

        val inputHash = hashService.hashPin(pin, storedSalt)
        inputHash == storedHash
    }

    private fun com.imbavchenko.bimil.db.App_settings.toDomainModel() = AppSettings(
        theme = try { Theme.valueOf(theme) } catch (e: Exception) { Theme.SYSTEM },
        language = language,
        region = try { Region.valueOf(region) } catch (e: Exception) { Region.GLOBAL },
        isPinEnabled = pin_hash != null,
        isBiometricEnabled = biometric_enabled == 1L,
        autoLockSeconds = auto_lock_seconds.toInt(),
        schemaVersion = schema_version.toInt()
    )
}
