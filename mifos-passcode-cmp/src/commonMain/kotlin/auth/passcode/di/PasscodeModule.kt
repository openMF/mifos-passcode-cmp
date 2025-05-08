package com.mifos.passcode.auth.passcode.di

import com.mifos.passcode.auth.passcode.data.repository.PasscodeRepositoryImpl
import com.mifos.passcode.auth.passcode.domain.PasscodeRepository
import com.mifos.passcode.auth.passcode.presentation.screen.PasscodeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


val PasscodeModule = module {
    single { PasscodeRepositoryImpl(get()) }.bind<PasscodeRepository>()
    viewModelOf(::PasscodeViewModel)
}