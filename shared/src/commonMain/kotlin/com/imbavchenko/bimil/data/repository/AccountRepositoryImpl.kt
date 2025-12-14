package com.imbavchenko.bimil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.imbavchenko.bimil.db.BimilDatabase
import com.imbavchenko.bimil.domain.model.AccountEntry
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.RequirementStatus
import com.imbavchenko.bimil.domain.model.SsoHint
import com.imbavchenko.bimil.domain.model.SsoProvider
import com.imbavchenko.bimil.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class AccountRepositoryImpl(
    private val database: BimilDatabase
) : AccountRepository {

    private val accountQueries = database.accountEntryQueries
    private val ssoQueries = database.ssoHintQueries
    private val passwordQueries = database.passwordHintQueries

    override fun getAllAccounts(): Flow<List<AccountEntry>> {
        return accountQueries.getAllAccounts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAccountById(id: String): Flow<AccountEntry?> {
        return accountQueries.getAccountById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainModel() }
    }

    override fun getAccountsByCategory(categoryId: String): Flow<List<AccountEntry>> {
        return accountQueries.getAccountsByCategory(categoryId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getFavoriteAccounts(): Flow<List<AccountEntry>> {
        return accountQueries.getFavoriteAccounts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun searchAccounts(query: String): Flow<List<AccountEntry>> {
        return accountQueries.searchAccounts(query, query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAccountCount(): Flow<Long> {
        return accountQueries.getAccountCount()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it ?: 0L }
    }

    override suspend fun insertAccount(account: AccountEntry) = withContext(Dispatchers.IO) {
        accountQueries.insertAccount(
            id = account.id,
            service_name = account.serviceName,
            username = account.username,
            website_url = account.websiteUrl,
            login_type = account.loginType.name,
            category_id = account.categoryId,
            is_favorite = if (account.isFavorite) 1L else 0L,
            memo = account.memo,
            icon_data = account.iconData,
            created_at = account.createdAt,
            updated_at = account.updatedAt
        )
    }

    override suspend fun updateAccount(account: AccountEntry) = withContext(Dispatchers.IO) {
        accountQueries.updateAccount(
            service_name = account.serviceName,
            username = account.username,
            website_url = account.websiteUrl,
            login_type = account.loginType.name,
            category_id = account.categoryId,
            is_favorite = if (account.isFavorite) 1L else 0L,
            memo = account.memo,
            icon_data = account.iconData,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = account.id
        )
    }

    override suspend fun updateFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        accountQueries.updateFavorite(
            is_favorite = if (isFavorite) 1L else 0L,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = id
        )
    }

    override suspend fun updateViewCount(id: String) = withContext(Dispatchers.IO) {
        accountQueries.updateViewCount(
            last_viewed_at = Clock.System.now().toEpochMilliseconds(),
            id = id
        )
    }

    override suspend fun deleteAccount(id: String) = withContext(Dispatchers.IO) {
        accountQueries.deleteAccount(id)
    }

    override suspend fun deleteAllAccounts() = withContext(Dispatchers.IO) {
        accountQueries.deleteAllAccounts()
    }

    // SSO Hint
    override fun getSsoHintByAccountId(accountId: String): Flow<SsoHint?> {
        return ssoQueries.getSsoHintByAccountId(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainModel() }
    }

    override suspend fun insertSsoHint(ssoHint: SsoHint) = withContext(Dispatchers.IO) {
        ssoQueries.insertSsoHint(
            id = ssoHint.id,
            account_id = ssoHint.accountId,
            provider = ssoHint.provider.name,
            provider_custom = ssoHint.providerCustom,
            created_at = ssoHint.createdAt
        )
    }

    override suspend fun updateSsoHint(ssoHint: SsoHint) = withContext(Dispatchers.IO) {
        ssoQueries.updateSsoHint(
            provider = ssoHint.provider.name,
            provider_custom = ssoHint.providerCustom,
            account_id = ssoHint.accountId
        )
    }

    override suspend fun deleteSsoHint(accountId: String) = withContext(Dispatchers.IO) {
        ssoQueries.deleteSsoHint(accountId)
    }

    // Password Hint
    override fun getPasswordHintByAccountId(accountId: String): Flow<PasswordHint?> {
        return passwordQueries.getPasswordHintByAccountId(accountId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainModel() }
    }

    override suspend fun insertPasswordHint(passwordHint: PasswordHint) = withContext(Dispatchers.IO) {
        passwordQueries.insertPasswordHint(
            id = passwordHint.id,
            account_id = passwordHint.accountId,
            min_length = passwordHint.minLength.toLong(),
            max_length = passwordHint.maxLength?.toLong(),
            requires_special = passwordHint.requiresSpecial.name,
            requires_uppercase = passwordHint.requiresUppercase.name,
            requires_lowercase = passwordHint.requiresLowercase.name,
            requires_number = passwordHint.requiresNumber.name,
            allowed_special_chars = passwordHint.allowedSpecialChars,
            personal_hint = passwordHint.personalHint,
            created_at = passwordHint.createdAt,
            updated_at = passwordHint.updatedAt
        )
    }

    override suspend fun updatePasswordHint(passwordHint: PasswordHint) = withContext(Dispatchers.IO) {
        passwordQueries.updatePasswordHint(
            min_length = passwordHint.minLength.toLong(),
            max_length = passwordHint.maxLength?.toLong(),
            requires_special = passwordHint.requiresSpecial.name,
            requires_uppercase = passwordHint.requiresUppercase.name,
            requires_lowercase = passwordHint.requiresLowercase.name,
            requires_number = passwordHint.requiresNumber.name,
            allowed_special_chars = passwordHint.allowedSpecialChars,
            personal_hint = passwordHint.personalHint,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            account_id = passwordHint.accountId
        )
    }

    override suspend fun deletePasswordHint(accountId: String) = withContext(Dispatchers.IO) {
        passwordQueries.deletePasswordHint(accountId)
    }

    // Combined
    override fun getAccountWithHint(id: String): Flow<AccountWithHint?> {
        return combine(
            getAccountById(id),
            getSsoHintByAccountId(id),
            getPasswordHintByAccountId(id)
        ) { account, ssoHint, passwordHint ->
            account?.let {
                AccountWithHint(
                    account = it,
                    ssoHint = ssoHint,
                    passwordHint = passwordHint
                )
            }
        }
    }

    override fun getAllAccountsWithHints(): Flow<List<AccountWithHint>> {
        return getAllAccounts().map { accounts ->
            accounts.map { account ->
                AccountWithHint(account = account)
            }
        }
    }

    // Extension functions for mapping
    private fun com.imbavchenko.bimil.db.Account_entry.toDomainModel() = AccountEntry(
        id = id,
        serviceName = service_name,
        username = username,
        websiteUrl = website_url,
        loginType = LoginType.valueOf(login_type),
        categoryId = category_id,
        isFavorite = is_favorite == 1L,
        memo = memo,
        iconData = icon_data,
        viewCount = view_count.toInt(),
        createdAt = created_at,
        updatedAt = updated_at,
        lastViewedAt = last_viewed_at
    )

    private fun com.imbavchenko.bimil.db.Sso_hint.toDomainModel() = SsoHint(
        id = id,
        accountId = account_id,
        provider = try { SsoProvider.valueOf(provider) } catch (e: Exception) { SsoProvider.CUSTOM },
        providerCustom = provider_custom,
        createdAt = created_at
    )

    private fun com.imbavchenko.bimil.db.Password_hint.toDomainModel() = PasswordHint(
        id = id,
        accountId = account_id,
        minLength = min_length.toInt(),
        maxLength = max_length?.toInt(),
        requiresSpecial = RequirementStatus.valueOf(requires_special),
        requiresUppercase = RequirementStatus.valueOf(requires_uppercase),
        requiresLowercase = RequirementStatus.valueOf(requires_lowercase),
        requiresNumber = RequirementStatus.valueOf(requires_number),
        allowedSpecialChars = allowed_special_chars,
        personalHint = personal_hint,
        createdAt = created_at,
        updatedAt = updated_at
    )
}
