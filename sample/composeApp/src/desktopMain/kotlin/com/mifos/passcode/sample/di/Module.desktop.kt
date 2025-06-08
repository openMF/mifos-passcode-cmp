package com.mifos.passcode.sample.di

import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreen
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        singleOf(::ChooseAuthOptionScreenViewmodel)
    }