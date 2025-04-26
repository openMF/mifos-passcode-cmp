package com.mifos.passcode.sample.di

import com.mifos.passcode.passcode.data.repository.PasscodeRepositoryImpl
import com.mifos.passcode.passcode.domain.PasscodeRepository
import com.mifos.passcode.ui.viewmodels.PasscodeViewModel
import com.mifos.passcode.passcode.data.database.PreferenceManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


expect val platformModule: Module

val sharedModule = module {
    singleOf(constructor = ::PasscodeRepositoryImpl).bind<PasscodeRepository>()
    singleOf(constructor = ::PreferenceManager)

    viewModelOf(::PasscodeViewModel)
}