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

import com.russhwolf.settings.Settings
import org.mifos.authenticator.biometrics.BiometricStorageAdapter

private const val REGISTRATION_DATA_KEY = "org.mifos.authenticator.registration_data"

class BiometricStorageAdapterImpl(
    private val settings: Settings,
) : BiometricStorageAdapter {
    override fun saveRegistrationData(registrationData: String) {
        settings.putString(REGISTRATION_DATA_KEY, registrationData)
    }

    override fun loadRegistrationData(): String? {
        return settings.getStringOrNull(REGISTRATION_DATA_KEY)
    }

    override fun deleteRegistrationData() {
        settings.remove(REGISTRATION_DATA_KEY)
    }
}
