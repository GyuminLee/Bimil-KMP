package com.imbavchenko.bimil.domain.usecase

import com.imbavchenko.bimil.domain.repository.AccountRepository
import com.imbavchenko.bimil.domain.repository.CategoryRepository

class DeleteAllDataUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke() {
        // Delete all accounts (this will cascade to hints)
        accountRepository.deleteAllAccounts()
        // Delete all non-default categories
        categoryRepository.deleteAllNonDefaultCategories()
    }
}
