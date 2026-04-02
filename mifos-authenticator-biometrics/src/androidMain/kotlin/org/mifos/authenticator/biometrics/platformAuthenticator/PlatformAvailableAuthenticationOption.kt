/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
@file:Suppress("PropertyName")

package org.mifos.authenticator.biometrics.platformAuthenticator

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformAvailableAuthenticationOption private actual constructor() {
    private var context: Context? = null
    actual constructor(context: Any?) : this() {
        this.context = context as? Context
        _currentAuthOptions.value = getAuthOption()
    }

    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> = _currentAuthOptions.asStateFlow()

    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = mutableListOf(PlatformAuthOptions.UserCredential)

        context?.let {
            val pm = it.packageManager

            var face = false
            var fingerprint = false
            var iris = false

            fingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                face = pm.hasSystemFeature(PackageManager.FEATURE_FACE)
                iris = pm.hasSystemFeature(PackageManager.FEATURE_IRIS)
            }
            if (face) availablePlatformAuthOptions.add(PlatformAuthOptions.FaceId)
            if (fingerprint) availablePlatformAuthOptions.add(PlatformAuthOptions.Fingerprint)
            if (iris) availablePlatformAuthOptions.add(PlatformAuthOptions.Iris)

            Logger.d { "Does the device have fingerprint lock? $fingerprint" }
            Logger.d { "Does the device have face lock? $face" }
            Logger.d { "Does the device have iris lock? $iris" }
        }

        return availablePlatformAuthOptions
    }
}
