package com.imbavchenko.bimil.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.VerifyPinUseCase
import com.imbavchenko.bimil.presentation.localization.Language
import com.imbavchenko.bimil.presentation.localization.ProvideStrings
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.navigation.Screen
import com.imbavchenko.bimil.presentation.screen.AddEditAccountScreen
import com.imbavchenko.bimil.presentation.screen.HomeScreen
import com.imbavchenko.bimil.presentation.screen.LockScreen
import com.imbavchenko.bimil.presentation.screen.SettingsScreen
import com.imbavchenko.bimil.presentation.theme.BimilTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BimilApp(
    getSettingsUseCase: GetSettingsUseCase = koinInject(),
    verifyPinUseCase: VerifyPinUseCase = koinInject(),
    biometricService: BiometricService = koinInject()
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
    val scope = rememberCoroutineScope()

    // Lock state - starts locked if PIN is enabled
    var isLocked by remember { mutableStateOf(true) }
    var hasUnlockedOnce by remember { mutableStateOf(false) }

    // Check if we should show lock screen
    val isPinEnabled = settings?.isPinEnabled == true
    val isBiometricEnabled = settings?.isBiometricEnabled == true
    val isBiometricAvailable = remember { biometricService.isBiometricAvailable() }

    // Handle lock state based on PIN setting
    LaunchedEffect(isPinEnabled) {
        if (!isPinEnabled) {
            // PIN disabled - unlock
            isLocked = false
            hasUnlockedOnce = false
        } else if (!hasUnlockedOnce) {
            // PIN enabled and never unlocked in this session - stay locked
            isLocked = true
        }
        // If PIN is enabled but user already unlocked once, stay unlocked until app restart
    }

    val isDarkTheme = when (settings?.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> null // System default
    }

    val language = when (settings?.language) {
        "ko" -> Language.KOREAN
        "ja" -> Language.JAPANESE
        "zh" -> Language.CHINESE
        "de" -> Language.GERMAN
        else -> Language.ENGLISH
    }

    BimilTheme(darkTheme = isDarkTheme ?: false) {
        ProvideStrings(language = language) {
            Surface(modifier = Modifier.fillMaxSize()) {
                // Show lock screen if locked and PIN is enabled
                if (isLocked && isPinEnabled) {
                    LockScreenContent(
                        verifyPinUseCase = verifyPinUseCase,
                        biometricService = biometricService,
                        isBiometricAvailable = isBiometricAvailable,
                        isBiometricEnabled = isBiometricEnabled,
                        onUnlock = {
                            isLocked = false
                            hasUnlockedOnce = true
                        }
                    )
                } else {
                    // Main app content
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
    }
}

@Composable
private fun LockScreenContent(
    verifyPinUseCase: VerifyPinUseCase,
    biometricService: BiometricService,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onUnlock: () -> Unit
) {
    val strings = strings()
    val scope = rememberCoroutineScope()

    // Auto-trigger biometric on first load if available and enabled
    LaunchedEffect(Unit) {
        if (isBiometricAvailable && isBiometricEnabled) {
            biometricService.authenticate(
                title = strings.biometricPromptTitle,
                subtitle = strings.biometricPromptSubtitle,
                onSuccess = onUnlock,
                onError = { /* User can try PIN */ },
                onCancel = { /* User can try PIN */ }
            )
        }
    }

    LockScreen(
        onUnlock = onUnlock,
        onVerifyPin = { pin -> verifyPinUseCase(pin) },
        onBiometricClick = {
            scope.launch {
                biometricService.authenticate(
                    title = strings.biometricPromptTitle,
                    subtitle = strings.biometricPromptSubtitle,
                    onSuccess = onUnlock,
                    onError = { /* Show error or let user try PIN */ },
                    onCancel = { /* User cancelled */ }
                )
            }
        },
        isBiometricAvailable = isBiometricAvailable,
        isBiometricEnabled = isBiometricEnabled
    )
}
