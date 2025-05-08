package com.mifos.passcode.core.di

import com.mifos.passcode.core.MifosDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


val DispatchersModule = module {
    includes(ioDispatcherModule)
    single<CoroutineDispatcher>(named(MifosDispatchers.Default.name)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(MifosDispatchers.Unconfined.name)) { Dispatchers.Unconfined }
    single<CoroutineScope>(named("ApplicationScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

expect val ioDispatcherModule: Module
