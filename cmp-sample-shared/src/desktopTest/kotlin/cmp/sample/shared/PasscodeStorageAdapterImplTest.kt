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

import com.russhwolf.settings.PropertiesSettings
import org.mifos.authenticator.passcode.PasscodeAttemptState
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

private const val ATTEMPT_STATE_KEY = "org.mifos.authenticator.passcode.attemptState"

class PasscodeStorageAdapterImplTest {
    @Test
    fun roundTripsAttemptState() {
        val storage = PasscodeStorageAdapterImpl(PropertiesSettings(Properties()))
        val expected = PasscodeAttemptState(
            failedAttempts = 2,
            lockoutCount = 1,
            lockedUntilEpochMillis = 12_345L,
        )

        storage.savePasscodeAttemptState(expected)

        assertEquals(expected, storage.loadPasscodeAttemptState())
    }

    @Test
    fun removesMalformedAttemptState() {
        val settings = PropertiesSettings(Properties()).apply {
            putString(ATTEMPT_STATE_KEY, "not-json")
        }
        val storage = PasscodeStorageAdapterImpl(settings)

        assertNull(storage.loadPasscodeAttemptState())
        assertFalse(settings.hasKey(ATTEMPT_STATE_KEY))
    }

    @Test
    fun removesAttemptStateFromUnsupportedVersion() {
        val settings = PropertiesSettings(Properties()).apply {
            putString(
                ATTEMPT_STATE_KEY,
                """{"version":2,"failedAttempts":2,"lockoutCount":1,"lockedUntilEpochMillis":12345}""",
            )
        }
        val storage = PasscodeStorageAdapterImpl(settings)

        assertNull(storage.loadPasscodeAttemptState())
        assertFalse(settings.hasKey(ATTEMPT_STATE_KEY))
    }
}
