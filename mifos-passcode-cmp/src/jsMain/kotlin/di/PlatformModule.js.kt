package com.mifos.passcode.di

import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.demoImpl.DemoAuthOption
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


actual val PlatformModule: Module
    get() = module {
        singleOf(::DemoAuthOption).bind<AuthOption>()
    }