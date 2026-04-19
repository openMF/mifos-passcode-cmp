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
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption

val libraryLocalAndroidActivity: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
val libraryLocalContextProvider: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

@Composable
expect fun PlatformAuthenticatorCompositionProvider(
    biometricStorageAdapter: BiometricStorageAdapter,
    content: @Composable () -> Unit,
)

val platformAuthenticationProvider: ProvidableCompositionLocal<PlatformAuthenticationProvider> =
    compositionLocalOf {
        error("CompositionLocal of PlatformAuthenticationProvider not provided")
    }

val platformAvailableAuthenticationOption: ProvidableCompositionLocal<PlatformAvailableAuthenticationOption> =
    compositionLocalOf {
        error("CompositionLocal of PlatformAvailableAuthenticationOption not provided")
    }
