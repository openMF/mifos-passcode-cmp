package com.mifos.passcode.sample.di

import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


val sharedModule: Module = module {
    singleOf(::PreferenceDataStoreImpl).bind<PreferenceDataStore>()
    singleOf(::ChooseAuthOptionRepository)

}

expect val platformModule: Module