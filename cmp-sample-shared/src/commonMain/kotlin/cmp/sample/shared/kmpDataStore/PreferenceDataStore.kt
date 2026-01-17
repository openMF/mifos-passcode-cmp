/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package cmp.sample.shared.kmpDataStore

interface PreferenceDataStore {

    fun <T> putData(key: String, value: T)

    fun <T> getSavedData(key: String, defaultValue: T): T

    fun clearData(key: String)
}
