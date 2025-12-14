package com.imbavchenko.bimil.domain.usecase

import com.imbavchenko.bimil.domain.model.AccountEntry
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.SsoHint
import com.imbavchenko.bimil.domain.repository.AccountRepository
import com.imbavchenko.bimil.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class GetAllAccountsUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<AccountWithHint>> {
        return combine(
            accountRepository.getAllAccounts(),
            categoryRepository.getAllCategories()
        ) { accounts, categories ->
            accounts.map { account ->
                AccountWithHint(
                    account = account,
                    category = categories.find { it.id == account.categoryId },
                    // Hints will be loaded via separate flow - see below
                    ssoHint = null,
                    passwordHint = null
                )
            }
        }.map { accountsWithHints ->
            // Load hints for each account
            accountsWithHints.map { awh ->
                val ssoHint = accountRepository.getSsoHintByAccountId(awh.account.id).first()
                val passwordHint = accountRepository.getPasswordHintByAccountId(awh.account.id).first()
                awh.copy(
                    ssoHint = ssoHint,
                    passwordHint = passwordHint
                )
            }
        }
    }
}

class GetAccountWithHintUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(id: String): Flow<AccountWithHint?> {
        return combine(
            accountRepository.getAccountById(id),
            accountRepository.getSsoHintByAccountId(id),
            accountRepository.getPasswordHintByAccountId(id),
            categoryRepository.getAllCategories()
        ) { account, ssoHint, passwordHint, categories ->
            account?.let {
                AccountWithHint(
                    account = it,
                    ssoHint = ssoHint,
                    passwordHint = passwordHint,
                    category = categories.find { cat -> cat.id == it.categoryId }
                )
            }
        }
    }
}

class SearchAccountsUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(query: String): Flow<List<AccountWithHint>> {
        val accountsFlow = if (query.isBlank()) {
            accountRepository.getAllAccounts()
        } else {
            accountRepository.searchAccounts(query)
        }

        return combine(
            accountsFlow,
            categoryRepository.getAllCategories()
        ) { accounts, categories ->
            accounts.map { account ->
                AccountWithHint(
                    account = account,
                    category = categories.find { it.id == account.categoryId },
                    ssoHint = null,
                    passwordHint = null
                )
            }
        }.map { accountsWithHints ->
            // Load hints for each account
            accountsWithHints.map { awh ->
                val ssoHint = accountRepository.getSsoHintByAccountId(awh.account.id).first()
                val passwordHint = accountRepository.getPasswordHintByAccountId(awh.account.id).first()
                awh.copy(
                    ssoHint = ssoHint,
                    passwordHint = passwordHint
                )
            }
        }
    }
}

class SaveAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(
        account: AccountEntry,
        ssoHint: SsoHint?,
        passwordHint: PasswordHint?
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val existingAccount = accountRepository.getAccountById(account.id).first()

        if (existingAccount == null) {
            // Insert new account
            accountRepository.insertAccount(account.copy(createdAt = now, updatedAt = now))

            when (account.loginType) {
                LoginType.SSO -> ssoHint?.let {
                    accountRepository.insertSsoHint(it.copy(createdAt = now))
                }
                LoginType.PASSWORD -> passwordHint?.let {
                    accountRepository.insertPasswordHint(it.copy(createdAt = now, updatedAt = now))
                }
            }
        } else {
            // Update existing account
            accountRepository.updateAccount(account.copy(updatedAt = now))

            // Update or insert hints based on login type
            when (account.loginType) {
                LoginType.SSO -> {
                    accountRepository.deletePasswordHint(account.id)
                    ssoHint?.let {
                        val existingSso = accountRepository.getSsoHintByAccountId(account.id).first()
                        if (existingSso != null) {
                            accountRepository.updateSsoHint(it)
                        } else {
                            accountRepository.insertSsoHint(it.copy(createdAt = now))
                        }
                    }
                }
                LoginType.PASSWORD -> {
                    accountRepository.deleteSsoHint(account.id)
                    passwordHint?.let {
                        val existingPw = accountRepository.getPasswordHintByAccountId(account.id).first()
                        if (existingPw != null) {
                            accountRepository.updatePasswordHint(it.copy(updatedAt = now))
                        } else {
                            accountRepository.insertPasswordHint(it.copy(createdAt = now, updatedAt = now))
                        }
                    }
                }
            }
        }
    }
}

class DeleteAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: String) {
        accountRepository.deleteSsoHint(id)
        accountRepository.deletePasswordHint(id)
        accountRepository.deleteAccount(id)
    }
}

class ToggleFavoriteUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: String, isFavorite: Boolean) {
        accountRepository.updateFavorite(id, isFavorite)
    }
}

class GetAccountCountUseCase(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): Flow<Long> = accountRepository.getAccountCount()
}
