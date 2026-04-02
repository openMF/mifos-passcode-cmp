/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.di

import cmp.sample.shared.BiometricStorageAdapterImpl
import cmp.sample.shared.PasscodeStorageAdapterImpl
import com.russhwolf.settings.Settings
import kotlinx.coroutines.MainScope
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

val passcodeModule = module {
    singleOf(::PasscodeStorageAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricStorageAdapterImpl).bind<BiometricStorageAdapter>()
    single {
        val biometricAdapter = get<BiometricStorageAdapter>()
        val isExternalAuthEnabled = biometricAdapter.loadRegistrationData() != null
        PasscodeManager(get(), MainScope()).initialize(isExternalAuthEnabled)
    }
    single { Settings() }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(passcodeModule)
    }
}
