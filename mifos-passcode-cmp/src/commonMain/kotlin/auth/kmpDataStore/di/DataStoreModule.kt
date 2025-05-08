package com.mifos.passcode.auth.kmpDataStore.di

import com.mifos.passcode.auth.kmpDataStore.data.repository.PreferencesDataSourceImpl
import com.mifos.passcode.auth.kmpDataStore.domain.PreferencesDataSource
import com.mifos.passcode.core.MifosDispatchers
import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


val DataStoreModule = module {
    factory<Settings> { Settings() }
    factory {
        PreferencesDataSourceImpl(get(), get(named(MifosDispatchers.IO.name)))
    }.bind<PreferencesDataSource>()
}