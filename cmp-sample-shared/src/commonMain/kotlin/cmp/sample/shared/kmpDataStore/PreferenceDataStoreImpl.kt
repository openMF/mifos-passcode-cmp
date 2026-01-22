/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.kmpDataStore

import com.russhwolf.settings.Settings

class PreferenceDataStoreImpl : PreferenceDataStore {
    private val settings: Settings by lazy {
        Settings()
    }

    override fun <T> putData(
        key: String,
        value: T,
    ) {
        if (value is String) {
            settings.putString(key, value)
        } else {
            settings.putInt(key, value as Int)
        }
    }

    override fun <T> getSavedData(
        key: String,
        defaultValue: T,
    ): T {
        return if (defaultValue is String) {
            settings.getString(key, defaultValue) as T
        } else {
            settings.getInt(key, defaultValue as Int) as T
        }
    }

    override fun clearData(key: String) {
        settings.remove(key)
    }
}
