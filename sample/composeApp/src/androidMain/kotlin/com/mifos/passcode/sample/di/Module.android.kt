package com.mifos.passcode.sample.di

import com.mifos.passcode.auth.AuthOption
import auth.AuthOptionAndroid
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


actual val platformModule: Module = module {

    singleOf(::AuthOptionAndroid).bind<AuthOption>()
}
