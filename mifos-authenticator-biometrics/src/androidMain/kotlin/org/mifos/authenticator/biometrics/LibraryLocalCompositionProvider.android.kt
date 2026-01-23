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

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption

@Composable
actual fun LibraryLocalCompositionProvider(
    content: @Composable (() -> Unit),
) {
    val activity = requireNotNull(LocalActivity.current) as FragmentActivity
    val contextLocal = LocalContext.current
    CompositionLocalProvider(
        libraryLocalAndroidActivity provides activity,
        libraryLocalContextProvider provides contextLocal,
        libraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(activity),
        libraryPlatformAvailableAuthenticationOption provides PlatformAvailableAuthenticationOption(
            contextLocal,
        ),
    ) {
        content()
    }
}
