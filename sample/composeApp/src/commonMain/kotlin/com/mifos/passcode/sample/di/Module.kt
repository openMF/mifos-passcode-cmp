package com.mifos.passcode.sample.di

import com.mifos.passcode.PasscodeRepository
import com.mifos.passcode.PasscodeRepositoryImpl
import com.mifos.passcode.utility.PreferenceManager
import com.mifos.passcode.viewmodels.BiometricAuthorizationViewModel
import com.mifos.passcode.viewmodels.PasscodeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module



val sharedModule = module {

    singleOf(constructor = ::PasscodeRepositoryImpl).bind<PasscodeRepository>()
    singleOf(constructor = ::PreferenceManager)

    viewModelOf(::PasscodeViewModel)
    viewModelOf(::BiometricAuthorizationViewModel)

}