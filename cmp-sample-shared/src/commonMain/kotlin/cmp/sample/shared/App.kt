/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionRepository
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import cmp.sample.shared.navigation.SampleAppNavigation
import cmp.sample.shared.platformAuthentication.AuthenticationScreenViewModel
import com.russhwolf.settings.Settings
import org.mifos.authenticator.biometrics.LibraryLocalCompositionProvider

@Composable
fun App() {
    LibraryLocalCompositionProvider {
        MaterialTheme {

            val settings = Settings()

            val chooseAuthOptionRepository = ChooseAuthOptionRepository(settings)
            val chooseAuthOptionScreenViewmodel = ChooseAuthOptionScreenViewmodel(
                chooseAuthOptionRepository,
            )

            val platformAuthOptionScreenViewmodel = AuthenticationScreenViewModel(
                chooseAuthOptionRepository = chooseAuthOptionRepository,
                settings = settings
            )

            val passcodeStorageAdapter = PasscodeStorageAdapterImpl(settings)

            SampleAppNavigation(
                passcodeStorageAdapter,
                chooseAuthOptionScreenViewmodel,
                platformAuthOptionScreenViewmodel,
            )
        }
    }
}
