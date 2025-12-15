package com.imbavchenko.bimil.di

import com.imbavchenko.bimil.data.ad.AdService
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.platform.DesktopAdService
import com.imbavchenko.bimil.platform.DesktopBiometricService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<BiometricService> { DesktopBiometricService() }
    single<AdService> { DesktopAdService() }
}
