package com.mifos.passcode.di

import auth.AuthOptionImplAndroid
import com.mifos.passcode.auth.AuthOption
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val PlatformModule: Module = module {
    singleOf(::AuthOptionImplAndroid).bind<AuthOption>()
}