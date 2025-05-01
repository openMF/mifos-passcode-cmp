package com.mifos.passcode.sample.di

import auth.preferenceDataStore.PreferenceDataStore
import auth.preferenceDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.auth.deviceAuth.data.repository.ChooseAuthOptionRepository
import com.mifos.passcode.auth.passcode.data.repository.PasscodeRepositoryImpl
import com.mifos.passcode.auth.passcode.domain.PasscodeRepository
import com.mifos.passcode.ui.viewmodels.ChooseAuthOptionViewModel
import com.mifos.passcode.ui.viewmodels.PasscodeViewModel
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


expect val platformModule: Module

val sharedModule = module {
    singleOf(constructor = ::PasscodeRepositoryImpl).bind<PasscodeRepository>()
    singleOf(constructor = ::ChooseAuthOptionRepository)
    singleOf(constructor = ::PreferenceDataStoreImpl).bind<PreferenceDataStore>()

    viewModelOf(::PasscodeViewModel)
    viewModelOf(::ChooseAuthOptionViewModel)
}