package com.imbavchenko.bimil.domain.repository

import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.AppSettings
import com.imbavchenko.bimil.domain.model.Category

interface BackupRepository {
    suspend fun createBackup(password: String): ByteArray
    suspend fun restoreBackup(data: ByteArray, password: String, merge: Boolean): RestoreResult
}

data class RestoreResult(
    val success: Boolean,
    val accountsRestored: Int,
    val categoriesRestored: Int,
    val error: String? = null
)

data class BackupData(
    val version: Int,
    val createdAt: Long,
    val settings: AppSettings,
    val categories: List<Category>,
    val accounts: List<AccountWithHint>
)
