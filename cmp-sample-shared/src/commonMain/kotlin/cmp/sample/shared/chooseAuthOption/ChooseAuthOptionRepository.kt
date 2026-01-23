/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.chooseAuthOption

import cmp.sample.shared.chooseAuthOption.utils.Helpers
import cmp.sample.shared.kmpDataStore.PreferenceDataStore

const val APP_LOCK_KEY = "auth_method"
const val REGISTRATION_DATA = "REGISTRATION_DATA"

class ChooseAuthOptionRepository(
    private val preferenceDataStore: PreferenceDataStore,
) {

    fun setAuthOption(option: AppLockOption) {
        preferenceDataStore.putData(
            APP_LOCK_KEY,
            Helpers.authOptionToStringMapperFunction(option),
        )
    }

    fun getAuthOption(): AppLockOption {
        return Helpers.stringToAuthOptionMapperFunction(
            preferenceDataStore.getSavedData(
                APP_LOCK_KEY,
                "",
            ),
        )
    }

    fun clearAuthOption() {
        preferenceDataStore.clearData(APP_LOCK_KEY)
    }

    fun saveRegistrationData(registrationData: String) {
        preferenceDataStore.putData(REGISTRATION_DATA, registrationData)
    }

    fun getRegistrationData() = preferenceDataStore.getSavedData(REGISTRATION_DATA, "")

    fun clearRegistrationData() {
        preferenceDataStore.clearData(REGISTRATION_DATA)
    }
}
