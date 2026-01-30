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
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

const val PASSCODE_KEY = "org.mifos.authenticator.passcode"

class PasscodeStorageAdapterImpl(
    private val settings: Settings,
) : PasscodeStorageAdapter {
    override fun savePasscode(passcode: String) {
        settings.putString(PASSCODE_KEY, passcode)
    }

    override fun loadPasscode(): String? {
        return settings.getStringOrNull(PASSCODE_KEY)
    }

    override fun deletePasscode() {
        settings.remove(PASSCODE_KEY)
    }
}
