package com.imbavchenko.bimil.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.presentation.navigation.Screen
import com.imbavchenko.bimil.presentation.screen.AddEditAccountScreen
import com.imbavchenko.bimil.presentation.screen.HomeScreen
import com.imbavchenko.bimil.presentation.screen.SettingsScreen
import com.imbavchenko.bimil.presentation.theme.BimilTheme
import org.koin.compose.koinInject

@Composable
fun BimilApp(
    getSettingsUseCase: GetSettingsUseCase = koinInject()
) {
    // Setup Coil3 ImageLoader with Ktor network support
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }

    val settings by getSettingsUseCase().collectAsState(initial = null)
    val isDarkTheme = when (settings?.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> null // System default
    }

    BimilTheme(darkTheme = isDarkTheme ?: false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Screen.Home
            ) {
                composable<Screen.Home> {
                    HomeScreen(
                        onNavigateToDetail = { accountId ->
                            navController.navigate(Screen.AddEditAccount(accountId))
                        },
                        onNavigateToAddAccount = {
                            navController.navigate(Screen.AddEditAccount(null))
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings)
                        }
                    )
                }

                composable<Screen.AddEditAccount> { backStackEntry ->
                    val route = backStackEntry.toRoute<Screen.AddEditAccount>()
                    AddEditAccountScreen(
                        accountId = route.accountId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.Settings> {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
