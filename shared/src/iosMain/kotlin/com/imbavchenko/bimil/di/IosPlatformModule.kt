package com.imbavchenko.bimil.di

import com.imbavchenko.bimil.data.ad.AdService
import com.imbavchenko.bimil.data.biometric.BiometricService
import com.imbavchenko.bimil.platform.IosAdService
import com.imbavchenko.bimil.platform.IosBiometricService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<BiometricService> { IosBiometricService() }
    single<AdService> { IosAdService() }
}
