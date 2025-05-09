package com.mifos.passcode.sample.di

import com.mifos.passcode.auth.chooseAppLock.di.ChooseAuthOptionModule
import com.mifos.passcode.auth.deviceAuth.di.DeviceAuthModule
import com.mifos.passcode.auth.kmpDataStore.di.DataStoreModule
import com.mifos.passcode.auth.passcode.di.PasscodeModule
import com.mifos.passcode.di.PlatformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            PlatformModule,
            DeviceAuthModule,
            ChooseAuthOptionModule,
            PasscodeModule,
            DataStoreModule
        )
    }
}