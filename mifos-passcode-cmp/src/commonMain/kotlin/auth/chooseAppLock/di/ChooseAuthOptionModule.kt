package com.mifos.passcode.auth.chooseAppLock.di

import com.mifos.passcode.auth.chooseAppLock.data.repository.ChooseAuthOptionRepository
import com.mifos.passcode.auth.chooseAppLock.presentation.ChooseAuthOptionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ChooseAuthOptionModule = module {
    singleOf(::ChooseAuthOptionRepository)
    viewModelOf(::ChooseAuthOptionViewModel)
}