package com.mifos.passcode.sample.di

import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.demoImpl.DemoAuthOption
import com.mifos.passcode.auth.demoImpl.DemoAuthenticator
import com.mifos.passcode.auth.deviceAuth.domain.PlatformAuthenticator
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::DemoAuthOption).bind<AuthOption>()
    singleOf(::DemoAuthenticator).bind<PlatformAuthenticator>()
}