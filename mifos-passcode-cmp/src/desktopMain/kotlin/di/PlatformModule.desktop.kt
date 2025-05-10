package com.mifos.passcode.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val PlatformModule: Module
    get() = module {
        includes(DemoImplModule)
    }