package com.mifos.passcode.auth.deviceAuth.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initDeviceAuthModule(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(DeviceAuthModule)
    }
}