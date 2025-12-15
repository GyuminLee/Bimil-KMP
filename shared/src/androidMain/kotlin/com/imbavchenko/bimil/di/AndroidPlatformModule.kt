package com.imbavchenko.bimil.di

import com.imbavchenko.bimil.data.ad.AdService
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.platform.AndroidAdService
import com.imbavchenko.bimil.platform.AndroidBiometricService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { ActivityProvider() }
    single<BiometricService> { AndroidBiometricService(get(), get()) }
    single<AdService> { AndroidAdService(get(), get()) }
}
