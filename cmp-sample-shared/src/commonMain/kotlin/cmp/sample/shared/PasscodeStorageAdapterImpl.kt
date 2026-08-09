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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mifos.authenticator.passcode.PasscodeAttemptState
import org.mifos.authenticator.passcode.PasscodeAttemptStorageAdapter
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

private const val PASSCODE_KEY = "org.mifos.authenticator.passcode"
private const val ATTEMPT_STATE_KEY = "org.mifos.authenticator.passcode.attemptState"
private const val ATTEMPT_STATE_VERSION = 1

private val attemptStateJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Serializable
private data class StoredPasscodeAttemptState(
    val version: Int = ATTEMPT_STATE_VERSION,
    val failedAttempts: Int,
    val lockoutCount: Int,
    val lockedUntilEpochMillis: Long? = null,
)

class PasscodeStorageAdapterImpl(
    private val settings: Settings,
) : PasscodeStorageAdapter, PasscodeAttemptStorageAdapter {
    override fun savePasscode(passcode: String) {
        settings.putString(PASSCODE_KEY, passcode)
    }

    override fun loadPasscode(): String? {
        return settings.getStringOrNull(PASSCODE_KEY)
    }

    override fun deletePasscode() {
        settings.remove(PASSCODE_KEY)
    }

    override fun savePasscodeAttemptState(state: PasscodeAttemptState) {
        val storedState = StoredPasscodeAttemptState(
            failedAttempts = state.failedAttempts,
            lockoutCount = state.lockoutCount,
            lockedUntilEpochMillis = state.lockedUntilEpochMillis,
        )
        settings.putString(ATTEMPT_STATE_KEY, attemptStateJson.encodeToString(storedState))
    }

    override fun loadPasscodeAttemptState(): PasscodeAttemptState? {
        val encodedState = settings.getStringOrNull(ATTEMPT_STATE_KEY) ?: return null
        val storedState = runCatching {
            attemptStateJson.decodeFromString<StoredPasscodeAttemptState>(encodedState)
        }.getOrNull()

        if (storedState?.version != ATTEMPT_STATE_VERSION) {
            settings.remove(ATTEMPT_STATE_KEY)
            return null
        }

        return PasscodeAttemptState(
            failedAttempts = storedState.failedAttempts,
            lockoutCount = storedState.lockoutCount,
            lockedUntilEpochMillis = storedState.lockedUntilEpochMillis,
        )
    }

    override fun deletePasscodeAttemptState() {
        settings.remove(ATTEMPT_STATE_KEY)
    }
}
