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
import cmp.sample.shared.navigation.SampleAppNavigation
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.biometrics.PlatformAuthenticatorCompositionProvider

@Composable
fun App() {
    val biometricStorageAdapter = koinInject<BiometricStorageAdapter>()
    PlatformAuthenticatorCompositionProvider(
        biometricStorageAdapter = biometricStorageAdapter,
    ) {
        MaterialTheme {
            SampleAppNavigation()
        }
    }
}
