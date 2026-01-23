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
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider

@Composable
actual fun LibraryLocalCompositionProvider(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        libraryLocalAndroidActivity provides null,
        libraryLocalContextProvider provides null,
        libraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(),
        libraryPlatformAvailableAuthenticationOption provides _root_ide_package_.org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption(),
    ) {
        content()
    }
}
