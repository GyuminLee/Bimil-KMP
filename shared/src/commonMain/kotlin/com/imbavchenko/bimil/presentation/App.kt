package com.imbavchenko.bimil.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.imbavchenko.bimil.data.ad.AdService
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.domain.model.Theme
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.VerifyPinUseCase
import com.imbavchenko.bimil.presentation.localization.Language
import com.imbavchenko.bimil.presentation.localization.ProvideStrings
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.navigation.Screen
import com.imbavchenko.bimil.presentation.component.rememberFilePickerLaunchers
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
    biometricService: BiometricService = koinInject(),
    adService: AdService = koinInject()
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

    // Initialize ad service
    LaunchedEffect(Unit) {
        adService.initialize()
    }

    val settings by getSettingsUseCase().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lock state - starts locked, will be unlocked only if PIN is disabled
    var isLocked by remember { mutableStateOf(true) }
    var wasInBackground by remember { mutableStateOf(false) }

    // Backup restore state - keep at app level to persist across lock/unlock
    var pendingRestoreData by remember { mutableStateOf<ByteArray?>(null) }

    // Check if settings are loaded and if PIN is enabled
    val settingsLoaded = settings != null
    val isPinEnabled = settings?.isPinEnabled == true
    val isBiometricEnabled = settings?.isBiometricEnabled == true
    // Don't use remember - check on every recomposition
    val isBiometricAvailable = biometricService.isBiometricAvailable()

    // Use rememberUpdatedState to capture latest isPinEnabled in callback
    val currentIsPinEnabled by rememberUpdatedState(isPinEnabled)

    // Handle lock state based on PIN setting - only unlock if settings loaded AND PIN disabled
    LaunchedEffect(settingsLoaded, isPinEnabled) {
        if (settingsLoaded && !isPinEnabled) {
            // Settings loaded and PIN is disabled - safe to unlock
            isLocked = false
        }
    }

    // Lock when app comes back from background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // App went to background
                    wasInBackground = true
                }
                Lifecycle.Event.ON_START -> {
                    // App came back from background - lock if PIN is enabled
                    if (wasInBackground && currentIsPinEnabled) {
                        isLocked = true
                    }
                    wasInBackground = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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

    // File picker launchers - must be at app level to persist across lock/unlock
    val filePickerLaunchers = rememberFilePickerLaunchers(
        onFileSaved = { success ->
            // File saved callback
        },
        onFileSelected = { data ->
            pendingRestoreData = data
        }
    )

    BimilTheme(darkTheme = isDarkTheme ?: false) {
        ProvideStrings(language = language) {
            Surface(modifier = Modifier.fillMaxSize()) {
                // Show lock screen only when settings are loaded AND PIN is enabled AND locked
                val shouldShowLockScreen = settingsLoaded && isPinEnabled && isLocked

                if (shouldShowLockScreen) {
                    LockScreenContent(
                        verifyPinUseCase = verifyPinUseCase,
                        biometricService = biometricService,
                        isBiometricAvailable = isBiometricAvailable,
                        isBiometricEnabled = isBiometricEnabled,
                        onUnlock = {
                            isLocked = false
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
                                },
                                onCreateBackup = { data, fileName ->
                                    filePickerLaunchers.launchFileSaver(data, fileName)
                                },
                                onSelectBackupFile = {
                                    filePickerLaunchers.launchFilePicker()
                                },
                                pendingRestoreData = pendingRestoreData,
                                onRestoreComplete = {
                                    pendingRestoreData = null
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
