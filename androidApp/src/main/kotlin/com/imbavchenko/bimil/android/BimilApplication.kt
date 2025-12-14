package com.imbavchenko.bimil.android

import android.app.Application
import com.imbavchenko.bimil.data.database.DatabaseDriverFactory
import com.imbavchenko.bimil.di.sharedModules
import com.imbavchenko.bimil.domain.usecase.InitializeCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.InitializeSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BimilApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BimilApplication)
            modules(
                listOf(androidModule) + sharedModules
            )
        }

        // Initialize database
        initializeDatabase()
    }

    private fun initializeDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            val initializeSettings: InitializeSettingsUseCase by inject()
            val initializeCategories: InitializeCategoriesUseCase by inject()

            initializeSettings()
            initializeCategories()
        }
    }
}

val androidModule = module {
    single { DatabaseDriverFactory(get()).createDriver() }
}
