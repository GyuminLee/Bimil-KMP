package com.imbavchenko.bimil.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.presentation.theme.BimilColors
import com.imbavchenko.bimil.domain.model.Category
import com.imbavchenko.bimil.presentation.component.AccountCard
import com.imbavchenko.bimil.presentation.component.BannerAd
import com.imbavchenko.bimil.presentation.component.SearchBar
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = strings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.appName,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settings
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAccount,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.addAccount,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = strings.searchServices
            )

            // Category filter chips
            CategoryChips(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = viewModel::onCategorySelected
            )

            // Main content area (takes remaining space)
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
            } else if (uiState.filteredAccounts.isEmpty()) {
                EmptyState(
                    hasSearchQuery = uiState.searchQuery.isNotBlank(),
                    onAddClick = onNavigateToAddAccount
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Favorites section
                    val favorites = uiState.filteredAccounts.filter { it.account.isFavorite }
                    if (favorites.isNotEmpty()) {
                        item {
                            Text(
                                text = strings.favorites,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(favorites, key = { it.account.id }) { accountWithHint ->
                            SwipeToDeleteAccountCard(
                                accountWithHint = accountWithHint,
                                onClick = { onNavigateToDetail(accountWithHint.account.id) },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(
                                        accountWithHint.account.id,
                                        accountWithHint.account.isFavorite
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteAccount(accountWithHint.account.id)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // All accounts
                    val nonFavorites = uiState.filteredAccounts.filter { !it.account.isFavorite }
                    if (nonFavorites.isNotEmpty()) {
                        item {
                            Text(
                                text = "${strings.all} (${uiState.filteredAccounts.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(nonFavorites, key = { it.account.id }) { accountWithHint ->
                            SwipeToDeleteAccountCard(
                                accountWithHint = accountWithHint,
                                onClick = { onNavigateToDetail(accountWithHint.account.id) },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(
                                        accountWithHint.account.id,
                                        accountWithHint.account.isFavorite
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteAccount(accountWithHint.account.id)
                                }
                            )
                        }
                    }

                    // Space for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
                }
            }

            // Banner ad at the bottom
            BannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteAccountCard(
    accountWithHint: AccountWithHint,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = strings()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
                false // Don't dismiss yet, wait for confirmation
            } else {
                false
            }
        }
    )

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                scope.launch {
                    dismissState.reset()
                }
            },
            title = {
                Text(text = strings.deleteAccount)
            },
            text = {
                Text(text = strings.deleteAccountConfirm.replace("%s", accountWithHint.account.serviceName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(strings.delete, color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            dismissState.reset()
                        }
                    }
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF5252)
                    else -> Color.Transparent
                },
                label = "delete_background_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            AccountCard(
                accountWithHint = accountWithHint,
                onClick = onClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    )
}

@Composable
private fun CategoryChips(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {
    val strings = strings()
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AssistChip(
                onClick = { onCategorySelected(null) },
                label = { Text(strings.all) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedCategoryId == null)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    labelColor = if (selectedCategoryId == null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }

        items(categories) { category ->
            AssistChip(
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedCategoryId == category.id)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    labelColor = if (selectedCategoryId == category.id)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasSearchQuery: Boolean,
    onAddClick: () -> Unit
) {
    val strings = strings()
    if (hasSearchQuery) {
        // Search results empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = strings.noResultsFound,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strings.tryDifferentSearch,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Welcome empty state with speech bubble
        WelcomeEmptyState()
    }
}

@Composable
private fun WelcomeEmptyState() {
    val strings = strings()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight

        // Speech bubble card positioned to point toward FAB area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp, end = 32.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Speech bubble card
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = BimilColors.Primary.copy(alpha = 0.1f),
                            spotColor = BimilColors.Primary.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.welcome,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.dp)
                                .background(
                                    BimilColors.Primary.copy(alpha = 0.5f),
                                    RoundedCornerShape(1.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = strings.addNewBimil,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BimilColors.Primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = strings.welcomeMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                        )
                    }
                }

                // Speech bubble tail pointing to FAB
                Box(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .offset(y = (-4).dp)
                ) {
                    // Triangle tail
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(45f)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}
