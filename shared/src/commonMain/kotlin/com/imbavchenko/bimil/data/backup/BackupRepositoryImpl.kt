package com.imbavchenko.bimil.data.backup

import com.imbavchenko.bimil.data.encryption.EncryptionService
import com.imbavchenko.bimil.domain.model.AccountEntry
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.SsoHint
import com.imbavchenko.bimil.domain.repository.AccountRepository
import com.imbavchenko.bimil.domain.repository.BackupRepository
import com.imbavchenko.bimil.domain.repository.CategoryRepository
import com.imbavchenko.bimil.domain.repository.RestoreResult
import com.imbavchenko.bimil.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupRepositoryImpl(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val encryptionService: EncryptionService
) : BackupRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override suspend fun createBackup(password: String): ByteArray {
        val settings = settingsRepository.getSettings().first()
        val categories = categoryRepository.getAllCategories().first()
        val accounts = accountRepository.getAllAccounts().first()

        val accountsWithHints = accounts.map { account ->
            val ssoHint = accountRepository.getSsoHintByAccountId(account.id).first()
            val passwordHint = accountRepository.getPasswordHintByAccountId(account.id).first()
            BackupAccountWithHint(
                account = account.toBackupModel(),
                ssoHint = ssoHint?.toBackupModel(),
                passwordHint = passwordHint?.toBackupModel()
            )
        }

        val backupFile = BackupFile(
            version = BACKUP_VERSION,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            appVersion = "1.0.0",
            settings = settings.toBackupModel(),
            categories = categories.map { it.toBackupModel() },
            accounts = accountsWithHints
        )

        val jsonData = json.encodeToString(backupFile)
        return encryptionService.encrypt(jsonData.encodeToByteArray(), password)
    }

    override suspend fun restoreBackup(data: ByteArray, password: String, merge: Boolean): RestoreResult {
        return try {
            val decrypted = encryptionService.decrypt(data, password)
            val jsonString = decrypted.decodeToString()
            val backupFile = json.decodeFromString<BackupFile>(jsonString)

            if (!merge) {
                // Clear existing data
                accountRepository.deleteAllAccounts()
            }

            // Restore categories
            var categoriesRestored = 0
            backupFile.categories.forEach { backupCategory ->
                try {
                    categoryRepository.insertCategory(backupCategory.toDomainModel())
                    categoriesRestored++
                } catch (e: Exception) {
                    // Category might already exist
                }
            }

            // Restore accounts
            var accountsRestored = 0
            backupFile.accounts.forEach { backupAccount ->
                try {
                    val account = backupAccount.account.toDomainModel()
                    accountRepository.insertAccount(account)

                    if (account.loginType == LoginType.SSO) {
                        backupAccount.ssoHint?.let {
                            accountRepository.insertSsoHint(it.toDomainModel(account.id))
                        }
                    } else {
                        backupAccount.passwordHint?.let {
                            accountRepository.insertPasswordHint(it.toDomainModel(account.id))
                        }
                    }
                    accountsRestored++
                } catch (e: Exception) {
                    // Account might already exist in merge mode
                }
            }

            RestoreResult(
                success = true,
                accountsRestored = accountsRestored,
                categoriesRestored = categoriesRestored
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                accountsRestored = 0,
                categoriesRestored = 0,
                error = e.message ?: "Unknown error"
            )
        }
    }

    companion object {
        const val BACKUP_VERSION = 1
    }
}

// Backup models
@Serializable
data class BackupFile(
    val version: Int,
    val createdAt: Long,
    val appVersion: String,
    val settings: BackupSettings,
    val categories: List<BackupCategory>,
    val accounts: List<BackupAccountWithHint>
)

@Serializable
data class BackupSettings(
    val theme: String,
    val language: String,
    val region: String
)

@Serializable
data class BackupCategory(
    val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val isDefault: Boolean,
    val sortOrder: Int
)

@Serializable
data class BackupAccountWithHint(
    val account: BackupAccount,
    val ssoHint: BackupSsoHint?,
    val passwordHint: BackupPasswordHint?
)

@Serializable
data class BackupAccount(
    val id: String,
    val serviceName: String,
    val username: String,
    val websiteUrl: String?,
    val loginType: String,
    val categoryId: String,
    val isFavorite: Boolean,
    val memo: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupSsoHint(
    val id: String,
    val provider: String,
    val providerCustom: String?
)

@Serializable
data class BackupPasswordHint(
    val id: String,
    val minLength: Int,
    val maxLength: Int?,
    val requiresSpecial: String,
    val requiresUppercase: String,
    val requiresLowercase: String,
    val requiresNumber: String,
    val allowedSpecialChars: String?,
    val personalHint: String?
)

// Extension functions for mapping
private fun AppSettings.toBackupModel() = BackupSettings(
    theme = theme.name,
    language = language,
    region = region.name
)

private fun Category.toBackupModel() = BackupCategory(
    id = id,
    name = name,
    color = color,
    icon = icon,
    isDefault = isDefault,
    sortOrder = sortOrder
)

private fun AccountEntry.toBackupModel() = BackupAccount(
    id = id,
    serviceName = serviceName,
    username = username,
    websiteUrl = websiteUrl,
    loginType = loginType.name,
    categoryId = categoryId,
    isFavorite = isFavorite,
    memo = memo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun SsoHint.toBackupModel() = BackupSsoHint(
    id = id,
    provider = provider.name,
    providerCustom = providerCustom
)

private fun PasswordHint.toBackupModel() = BackupPasswordHint(
    id = id,
    minLength = minLength,
    maxLength = maxLength,
    requiresSpecial = requiresSpecial.name,
    requiresUppercase = requiresUppercase.name,
    requiresLowercase = requiresLowercase.name,
    requiresNumber = requiresNumber.name,
    allowedSpecialChars = allowedSpecialChars,
    personalHint = personalHint
)

private fun BackupCategory.toDomainModel() = Category(
    id = id,
    name = name,
    color = color,
    icon = icon,
    isDefault = isDefault,
    sortOrder = sortOrder
)

private fun BackupAccount.toDomainModel() = AccountEntry(
    id = id,
    serviceName = serviceName,
    username = username,
    websiteUrl = websiteUrl,
    loginType = LoginType.valueOf(loginType),
    categoryId = categoryId,
    isFavorite = isFavorite,
    memo = memo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun BackupSsoHint.toDomainModel(accountId: String) = SsoHint(
    id = id,
    accountId = accountId,
    provider = com.imbavchenko.bimil.domain.model.SsoProvider.valueOf(provider),
    providerCustom = providerCustom
)

private fun BackupPasswordHint.toDomainModel(accountId: String) = PasswordHint(
    id = id,
    accountId = accountId,
    minLength = minLength,
    maxLength = maxLength,
    requiresSpecial = com.imbavchenko.bimil.domain.model.RequirementStatus.valueOf(requiresSpecial),
    requiresUppercase = com.imbavchenko.bimil.domain.model.RequirementStatus.valueOf(requiresUppercase),
    requiresLowercase = com.imbavchenko.bimil.domain.model.RequirementStatus.valueOf(requiresLowercase),
    requiresNumber = com.imbavchenko.bimil.domain.model.RequirementStatus.valueOf(requiresNumber),
    allowedSpecialChars = allowedSpecialChars,
    personalHint = personalHint
)
