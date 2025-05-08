package com.mifos.passcode.core


import org.koin.core.annotation.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val mifosDispatcher: MifosDispatchers)

enum class MifosDispatchers {
    Default,
    IO,
    Unconfined,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
