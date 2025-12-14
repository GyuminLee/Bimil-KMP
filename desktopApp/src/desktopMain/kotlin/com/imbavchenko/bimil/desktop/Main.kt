package com.imbavchenko.bimil.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.imbavchenko.bimil.data.database.DatabaseDriverFactory
import com.imbavchenko.bimil.di.sharedModules
import com.imbavchenko.bimil.domain.usecase.InitializeCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.InitializeSettingsUseCase
import com.imbavchenko.bimil.presentation.BimilApp
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(
            listOf(desktopModule) + sharedModules
        )
    }

    // Initialize database
    runBlocking {
        val koin = org.koin.core.context.GlobalContext.get()
        val initializeSettings = koin.get<InitializeSettingsUseCase>()
        val initializeCategories = koin.get<InitializeCategoriesUseCase>()

        initializeSettings()
        initializeCategories()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Bimil",
        state = rememberWindowState(
            width = 420.dp,
            height = 800.dp
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            BimilApp()
        }
    }
}

val desktopModule = module {
    single { DatabaseDriverFactory().createDriver() }
}
