/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.passcode

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Configures how failed passcode verification attempts are throttled.
 *
 * Each group of [maxFailedAttempts] failures starts a lockout. Durations progress through
 * [lockoutDurations], with the final duration reused for subsequent lockouts. A successful
 * verification resets the progression.
 */
class PasscodeBruteForcePolicy(
    val maxFailedAttempts: Int = 5,
    lockoutDurations: List<Duration> = listOf(30.seconds, 1.minutes, 5.minutes),
) {
    val lockoutDurations: List<Duration> = lockoutDurations.toList()

    init {
        require(maxFailedAttempts > 0) { "maxFailedAttempts must be greater than zero" }
        require(lockoutDurations.isNotEmpty()) { "lockoutDurations must not be empty" }
        require(lockoutDurations.all { it.isFinite() && it > Duration.ZERO && it.inWholeMilliseconds > 0 }) {
            "lockoutDurations must contain finite, positive durations of at least one millisecond"
        }
    }

    internal fun durationFor(lockoutCount: Int): Duration =
        lockoutDurations[lockoutCount.coerceIn(0, lockoutDurations.lastIndex)]
}

/**
 * Persistence state used to preserve brute-force protection across app restarts.
 *
 * @property failedAttempts Failed verifications since the previous lockout.
 * @property lockoutCount Lockouts since the last successful verification.
 * @property lockedUntilEpochMillis End of the active lockout, as Unix epoch milliseconds.
 */
data class PasscodeAttemptState(
    val failedAttempts: Int = 0,
    val lockoutCount: Int = 0,
    val lockedUntilEpochMillis: Long? = null,
)

/**
 * Persists failed-attempt state. Implement this alongside [PasscodeStorageAdapter] to keep
 * lockouts active after an app restart.
 */
interface PasscodeAttemptStorageAdapter {
    fun savePasscodeAttemptState(state: PasscodeAttemptState)

    fun loadPasscodeAttemptState(): PasscodeAttemptState?

    fun deletePasscodeAttemptState()
}

internal class InMemoryPasscodeAttemptStorageAdapter : PasscodeAttemptStorageAdapter {
    private var state: PasscodeAttemptState? = null

    override fun savePasscodeAttemptState(state: PasscodeAttemptState) {
        this.state = state
    }

    override fun loadPasscodeAttemptState(): PasscodeAttemptState? = state

    override fun deletePasscodeAttemptState() {
        state = null
    }
}
