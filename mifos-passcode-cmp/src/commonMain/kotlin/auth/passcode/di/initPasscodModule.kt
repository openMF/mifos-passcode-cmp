package com.mifos.passcode.auth.passcode.di

import com.mifos.passcode.auth.kmpDataStore.di.DataStoreModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


fun initPasscodeModule(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(PasscodeModule, DataStoreModule)
    }
}