package com.mifos.passcode.sample.di

import AndroidAuthenticator
import auth.AuthOptionAndroid
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.deviceAuth.domain.PlatformAuthenticator
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


actual val platformModule: Module = module {
    single<PlatformAuthenticator>{
        AndroidAuthenticator(androidContext())
    }
    singleOf(::AuthOptionAndroid).bind<AuthOption>()
}
