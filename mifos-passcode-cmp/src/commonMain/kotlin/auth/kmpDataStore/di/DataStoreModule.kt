package com.mifos.passcode.auth.kmpDataStore.di

import com.mifos.passcode.auth.kmpDataStore.PreferenceDataStore
import com.mifos.passcode.auth.kmpDataStore.PreferenceDataStoreImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DataStoreModule = module {
    singleOf(::PreferenceDataStoreImpl).bind<PreferenceDataStore>()
}