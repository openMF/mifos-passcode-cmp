/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticator
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption

@Composable
actual fun PlatformAuthenticatorLocalCompositionProvider(
    biometricStorageAdapter: BiometricStorageAdapter,
    content: @Composable (() -> Unit),
) {
    val provider = remember(biometricStorageAdapter) {
        PlatformAuthenticationProvider(
            authenticator = PlatformAuthenticator(),
            biometricStorageAdapter = biometricStorageAdapter,
        )
    }
    CompositionLocalProvider(
        libraryLocalAndroidActivity provides null,
        libraryLocalContextProvider provides null,
        platformAuthenticationProvider provides provider,
        platformAvailableAuthenticationOption provides PlatformAvailableAuthenticationOption(),
    ) {
        content()
    }
}
