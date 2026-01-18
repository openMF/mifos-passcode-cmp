/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
@file:Suppress("PropertyName")

package org.mifos.authenticator.biometrics.platformAuthenticator

import com.sun.jna.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mifos.authenticator.biometrics.windows.utils.isWindowsTenOrEleven

actual class PlatformAvailableAuthenticationOption private actual constructor() {

    actual constructor(context: Any?) : this() {
        _currentAuthOptions.value = getAuthOption()
    }

    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> = _currentAuthOptions.asStateFlow()

    private val isWindowsTenOrHigh = if (Platform.isWindows()) isWindowsTenOrEleven() else false
    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = if (isWindowsTenOrHigh) {
            listOf(
                PlatformAuthOptions.UserCredential,
                PlatformAuthOptions.Fingerprint,
                PlatformAuthOptions.FaceId,
                PlatformAuthOptions.Voice,
            )
        } else {
            emptyList()
        }

        return availablePlatformAuthOptions
    }
}
