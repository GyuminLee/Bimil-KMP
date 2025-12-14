package com.imbavchenko.bimil.domain.repository

import com.imbavchenko.bimil.domain.model.AccountEntry
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.SsoHint
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<AccountEntry>>
    fun getAccountById(id: String): Flow<AccountEntry?>
    fun getAccountsByCategory(categoryId: String): Flow<List<AccountEntry>>
    fun getFavoriteAccounts(): Flow<List<AccountEntry>>
    fun searchAccounts(query: String): Flow<List<AccountEntry>>
    fun getAccountCount(): Flow<Long>

    suspend fun insertAccount(account: AccountEntry)
    suspend fun updateAccount(account: AccountEntry)
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
    suspend fun updateViewCount(id: String)
    suspend fun deleteAccount(id: String)
    suspend fun deleteAllAccounts()

    // SSO Hint
    fun getSsoHintByAccountId(accountId: String): Flow<SsoHint?>
    suspend fun insertSsoHint(ssoHint: SsoHint)
    suspend fun updateSsoHint(ssoHint: SsoHint)
    suspend fun deleteSsoHint(accountId: String)

    // Password Hint
    fun getPasswordHintByAccountId(accountId: String): Flow<PasswordHint?>
    suspend fun insertPasswordHint(passwordHint: PasswordHint)
    suspend fun updatePasswordHint(passwordHint: PasswordHint)
    suspend fun deletePasswordHint(accountId: String)

    // Combined
    fun getAccountWithHint(id: String): Flow<AccountWithHint?>
    fun getAllAccountsWithHints(): Flow<List<AccountWithHint>>
}
