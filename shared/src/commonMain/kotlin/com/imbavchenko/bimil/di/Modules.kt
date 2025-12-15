package com.imbavchenko.bimil.di

import com.imbavchenko.bimil.data.backup.BackupRepositoryImpl
import com.imbavchenko.bimil.data.encryption.EncryptionService
import com.imbavchenko.bimil.data.encryption.HashService
import com.imbavchenko.bimil.data.repository.AccountRepositoryImpl
import com.imbavchenko.bimil.data.repository.CategoryRepositoryImpl
import com.imbavchenko.bimil.data.repository.SettingsRepositoryImpl
import com.imbavchenko.bimil.db.BimilDatabase
import com.imbavchenko.bimil.domain.repository.AccountRepository
import com.imbavchenko.bimil.domain.repository.BackupRepository
import com.imbavchenko.bimil.domain.repository.CategoryRepository
import com.imbavchenko.bimil.domain.repository.SettingsRepository
import com.imbavchenko.bimil.domain.usecase.ClearPinUseCase
import com.imbavchenko.bimil.domain.usecase.CreateBackupUseCase
import com.imbavchenko.bimil.domain.usecase.DeleteAccountUseCase
import com.imbavchenko.bimil.domain.usecase.DeleteCategoryUseCase
import com.imbavchenko.bimil.domain.usecase.GetAccountCountUseCase
import com.imbavchenko.bimil.domain.usecase.GetAccountWithHintUseCase
import com.imbavchenko.bimil.domain.usecase.GetAllAccountsUseCase
import com.imbavchenko.bimil.domain.usecase.GetAllCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.GetCategoryByIdUseCase
import com.imbavchenko.bimil.domain.usecase.GetSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.InitializeCategoriesUseCase
import com.imbavchenko.bimil.domain.usecase.InitializeSettingsUseCase
import com.imbavchenko.bimil.domain.usecase.RestoreBackupUseCase
import com.imbavchenko.bimil.domain.usecase.SaveAccountUseCase
import com.imbavchenko.bimil.domain.usecase.SaveCategoryUseCase
import com.imbavchenko.bimil.domain.usecase.SearchAccountsUseCase
import com.imbavchenko.bimil.domain.usecase.SetPinUseCase
import com.imbavchenko.bimil.domain.usecase.ToggleFavoriteUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateAutoLockUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateBiometricUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateLanguageUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateRegionUseCase
import com.imbavchenko.bimil.domain.usecase.UpdateThemeUseCase
import com.imbavchenko.bimil.domain.usecase.VerifyPinUseCase
import com.imbavchenko.bimil.presentation.viewmodel.AddEditAccountViewModel
import com.imbavchenko.bimil.presentation.viewmodel.HomeViewModel
import com.imbavchenko.bimil.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Services
    singleOf(::HashService)
    singleOf(::EncryptionService)

    // Database
    single { BimilDatabase(get()) }

    // Repositories
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::BackupRepositoryImpl) bind BackupRepository::class
}

val domainModule = module {
    // Account Use Cases
    factoryOf(::GetAllAccountsUseCase)
    factoryOf(::GetAccountWithHintUseCase)
    factoryOf(::SearchAccountsUseCase)
    factoryOf(::SaveAccountUseCase)
    factoryOf(::DeleteAccountUseCase)
    factoryOf(::ToggleFavoriteUseCase)
    factoryOf(::GetAccountCountUseCase)

    // Category Use Cases
    factoryOf(::GetAllCategoriesUseCase)
    factoryOf(::GetCategoryByIdUseCase)
    factoryOf(::SaveCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)
    factoryOf(::InitializeCategoriesUseCase)

    // Settings Use Cases
    factoryOf(::GetSettingsUseCase)
    factoryOf(::UpdateThemeUseCase)
    factoryOf(::UpdateLanguageUseCase)
    factoryOf(::UpdateRegionUseCase)
    factoryOf(::SetPinUseCase)
    factoryOf(::VerifyPinUseCase)
    factoryOf(::ClearPinUseCase)
    factoryOf(::UpdateBiometricUseCase)
    factoryOf(::UpdateAutoLockUseCase)
    factoryOf(::InitializeSettingsUseCase)

    // Backup Use Cases
    factoryOf(::CreateBackupUseCase)
    factoryOf(::RestoreBackupUseCase)
}

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddEditAccountViewModel)
    viewModelOf(::SettingsViewModel)
}

val sharedModules = listOf(dataModule, domainModule, presentationModule, platformModule)
