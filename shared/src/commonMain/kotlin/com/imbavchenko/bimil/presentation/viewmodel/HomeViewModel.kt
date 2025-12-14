package com.imbavchenko.bimil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.domain.model.SortOrder
import com.imbavchenko.bimil.domain.usecase.DeleteAccountUseCase
import com.imbavchenko.bimil.domain.usecase.GetAllAccountsUseCase
import com.imbavchenko.bimil.domain.usecase.GetAllCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.SearchAccountsUseCase
import com.imbavchenko.bimil.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val accounts: List<AccountWithHint> = emptyList(),
    val filteredAccounts: List<AccountWithHint> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Track accounts being toggled to prevent DB flow from overwriting optimistic updates
    val togglingFavoriteIds: Set<String> = emptySet()
)

class HomeViewModel(
    private val getAllAccountsUseCase: GetAllAccountsUseCase,
    private val searchAccountsUseCase: SearchAccountsUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getAllCategoriesUseCase().collectLatest { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            getAllAccountsUseCase().collectLatest { accountsFromDb ->
                _uiState.update { state ->
                    // Merge DB data with optimistic updates for accounts being toggled
                    val mergedAccounts = accountsFromDb.map { dbAccount ->
                        if (state.togglingFavoriteIds.contains(dbAccount.account.id)) {
                            // Keep the optimistic state for accounts being toggled
                            state.accounts.find { it.account.id == dbAccount.account.id } ?: dbAccount
                        } else {
                            dbAccount
                        }
                    }
                    state.copy(
                        accounts = mergedAccounts,
                        filteredAccounts = filterAndSort(mergedAccounts, state.searchQuery, state.selectedCategoryId, state.sortOrder),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredAccounts = filterAndSort(state.accounts, query, state.selectedCategoryId, state.sortOrder)
            )
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedCategoryId = categoryId,
                filteredAccounts = filterAndSort(state.accounts, state.searchQuery, categoryId, state.sortOrder)
            )
        }
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _uiState.update { state ->
            state.copy(
                sortOrder = sortOrder,
                filteredAccounts = filterAndSort(state.accounts, state.searchQuery, state.selectedCategoryId, sortOrder)
            )
        }
    }

    fun toggleFavorite(accountId: String, currentFavoriteFromUI: Boolean) {
        // Get the actual current state from our state to avoid stale closure values
        val currentState = _uiState.value
        val actualCurrentFavorite = currentState.accounts
            .find { it.account.id == accountId }
            ?.account?.isFavorite ?: currentFavoriteFromUI

        val newFavoriteState = !actualCurrentFavorite

        // Mark this account as being toggled and update UI optimistically
        _uiState.update { state ->
            val updatedAccounts = state.accounts.map { accountWithHint ->
                if (accountWithHint.account.id == accountId) {
                    accountWithHint.copy(
                        account = accountWithHint.account.copy(isFavorite = newFavoriteState)
                    )
                } else {
                    accountWithHint
                }
            }
            state.copy(
                accounts = updatedAccounts,
                filteredAccounts = filterAndSort(updatedAccounts, state.searchQuery, state.selectedCategoryId, state.sortOrder),
                togglingFavoriteIds = state.togglingFavoriteIds + accountId
            )
        }

        // Persist to database, then clear the toggling flag
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(accountId, newFavoriteState)
            } finally {
                // Clear the toggling flag after DB operation completes
                _uiState.update { state ->
                    state.copy(togglingFavoriteIds = state.togglingFavoriteIds - accountId)
                }
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            deleteAccountUseCase(accountId)
        }
    }

    private fun filterAndSort(
        accounts: List<AccountWithHint>,
        query: String,
        categoryId: String?,
        sortOrder: SortOrder
    ): List<AccountWithHint> {
        return accounts
            .filter { accountWithHint ->
                val matchesSearch = query.isBlank() ||
                        accountWithHint.account.serviceName.contains(query, ignoreCase = true) ||
                        accountWithHint.account.username.contains(query, ignoreCase = true) ||
                        accountWithHint.account.memo?.contains(query, ignoreCase = true) == true

                val matchesCategory = categoryId == null ||
                        accountWithHint.account.categoryId == categoryId

                matchesSearch && matchesCategory
            }
            .let { filtered ->
                // Favorites first
                val (favorites, nonFavorites) = filtered.partition { it.account.isFavorite }

                val sortedFavorites = sortList(favorites, sortOrder)
                val sortedNonFavorites = sortList(nonFavorites, sortOrder)

                sortedFavorites + sortedNonFavorites
            }
    }

    private fun sortList(list: List<AccountWithHint>, sortOrder: SortOrder): List<AccountWithHint> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> list.sortedBy { it.account.serviceName.lowercase() }
            SortOrder.NAME_DESC -> list.sortedByDescending { it.account.serviceName.lowercase() }
            SortOrder.RECENT_MODIFIED -> list.sortedByDescending { it.account.updatedAt }
            SortOrder.MOST_VIEWED -> list.sortedByDescending { it.account.viewCount }
        }
    }
}
