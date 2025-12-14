package com.imbavchenko.bimil.domain.usecase

import com.imbavchenko.bimil.domain.repository.BackupRepository
import com.imbavchenko.bimil.domain.repository.RestoreResult

class CreateBackupUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(password: String): ByteArray {
        return backupRepository.createBackup(password)
    }
}

class RestoreBackupUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(data: ByteArray, password: String, merge: Boolean): RestoreResult {
        return backupRepository.restoreBackup(data, password, merge)
    }
}
