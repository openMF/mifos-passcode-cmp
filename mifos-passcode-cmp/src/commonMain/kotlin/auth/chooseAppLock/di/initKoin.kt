package com.mifos.passcode.auth.chooseAppLock.di

import com.mifos.passcode.auth.deviceAuth.di.DeviceAuthModule
import com.mifos.passcode.auth.kmpDataStore.di.DataStoreModule
import com.mifos.passcode.auth.passcode.di.PasscodeModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(DataStoreModule, PasscodeModule, DeviceAuthModule, ChooseAuthOptionModule)
    }
}