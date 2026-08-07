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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PasscodeManagerBruteForceTest {
    @Test
    fun passcodeStorageAlsoPersistsAttemptsWhenItImplementsBothAdapters() {
        val storage = TestStorage(passcode = PASSCODE)
        val manager = PasscodeManager(
            adapter = storage,
            bruteForcePolicy = PasscodeBruteForcePolicy(
                maxFailedAttempts = 3,
                lockoutDurations = listOf(1.seconds),
            ),
        )

        enterPasscode(manager, WRONG_PASSCODE)

        assertTrue(manager.isAttemptStatePersistent)
        assertEquals(1, storage.attemptState?.failedAttempts)
    }

    @Test
    fun legacyStorageUsesProcessLocalAttemptState() {
        val manager = PasscodeManager(LegacyStorage(passcode = PASSCODE))

        assertFalse(manager.isAttemptStatePersistent)
    }

    @Test
    fun lockoutPersistsAcrossManagerRecreationAndClearsAfterVerification() {
        val storage = TestStorage(passcode = PASSCODE)
        var now = 1_000L
        val policy = PasscodeBruteForcePolicy(
            maxFailedAttempts = 3,
            lockoutDurations = listOf(1.seconds, 5.seconds),
        )
        val results = mutableListOf<PasscodeResult>()
        val manager = createManager(storage, policy) { now }
        manager.setResultCallback(results::add)

        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)

        assertEquals(3, results.count { it == PasscodeResult.Rejected })
        assertEquals(1_000L, manager.remainingLockoutMillis())
        assertEquals(2_000L, storage.attemptState?.lockedUntilEpochMillis)

        val restartedResults = mutableListOf<PasscodeResult>()
        val restartedManager = createManager(storage, policy) { now }
        restartedManager.setResultCallback(restartedResults::add)
        restartedManager.enterKey("1")

        assertEquals(PasscodeResult.Rejected, restartedResults.single())
        assertEquals(1_000L, restartedManager.remainingLockoutMillis())
        assertEquals(0, restartedManager.state.value.filledDots)

        now = 2_000L
        enterPasscode(restartedManager, PASSCODE)

        assertEquals(PasscodeResult.Verified, restartedResults.last())
        assertNull(storage.attemptState)
    }

    @Test
    fun repeatedLockoutsUseProgressiveDurations() {
        val storage = TestStorage(passcode = PASSCODE)
        var now = 10_000L
        val policy = PasscodeBruteForcePolicy(
            maxFailedAttempts = 2,
            lockoutDurations = listOf(1.seconds, 5.seconds),
        )
        val results = mutableListOf<PasscodeResult>()
        val manager = createManager(storage, policy) { now }
        manager.setResultCallback(results::add)

        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)
        assertEquals(PasscodeResult.Rejected, results.last())
        assertEquals(1_000L, manager.remainingLockoutMillis())

        now += 1_000L
        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)
        assertEquals(PasscodeResult.Rejected, results.last())
        assertEquals(5_000L, manager.remainingLockoutMillis())

        now += 5_000L
        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)
        assertEquals(PasscodeResult.Rejected, results.last())
        assertEquals(5_000L, manager.remainingLockoutMillis())
        assertEquals(3, storage.attemptState?.lockoutCount)
    }

    @Test
    fun expiredLockoutStillEscalatesAfterManagerRecreation() {
        val storage = TestStorage(passcode = PASSCODE).apply {
            attemptState = PasscodeAttemptState(
                lockoutCount = 1,
                lockedUntilEpochMillis = 2_000L,
            )
        }
        val policy = PasscodeBruteForcePolicy(
            maxFailedAttempts = 2,
            lockoutDurations = listOf(1.seconds, 5.seconds),
        )
        val results = mutableListOf<PasscodeResult>()
        val manager = createManager(storage, policy) { 2_000L }
        manager.setResultCallback(results::add)

        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)

        assertEquals(PasscodeResult.Rejected, results.last())
        assertEquals(5_000L, manager.remainingLockoutMillis())
        assertEquals(2, storage.attemptState?.lockoutCount)
        assertEquals(7_000L, storage.attemptState?.lockedUntilEpochMillis)
    }

    @Test
    fun policyRejectsInfiniteLockoutDurations() {
        assertFailsWith<IllegalArgumentException> {
            PasscodeBruteForcePolicy(lockoutDurations = listOf(Duration.INFINITE))
        }
    }

    @Test
    fun activeLockoutDiscardsPartiallyPersistedFailedAttempts() {
        val storage = TestStorage(passcode = PASSCODE).apply {
            attemptState = PasscodeAttemptState(
                failedAttempts = Int.MAX_VALUE,
                lockoutCount = -1,
                lockedUntilEpochMillis = 2_000L,
            )
        }
        val results = mutableListOf<PasscodeResult>()
        val manager = createManager(
            storage = storage,
            policy = PasscodeBruteForcePolicy(maxFailedAttempts = 3, lockoutDurations = listOf(1.seconds)),
            currentTimeMillis = { 1_000L },
        )
        manager.setResultCallback(results::add)

        manager.enterKey("1")

        assertEquals(PasscodeAttemptState(lockedUntilEpochMillis = 2_000L), storage.attemptState)
        assertEquals(PasscodeResult.Rejected, results.single())
        assertEquals(1_000L, manager.remainingLockoutMillis())
        assertEquals(0, manager.state.value.filledDots)
    }

    @Test
    fun changePasscodeVerificationIsRateLimited() {
        val storage = TestStorage(passcode = PASSCODE)
        val results = mutableListOf<PasscodeResult>()
        val manager = createManager(
            storage = storage,
            policy = PasscodeBruteForcePolicy(maxFailedAttempts = 2, lockoutDurations = listOf(1.seconds)),
            currentTimeMillis = { 1_000L },
        )
        manager.setResultCallback(results::add)

        manager.changePasscode()
        enterPasscode(manager, WRONG_PASSCODE)
        enterPasscode(manager, WRONG_PASSCODE)

        assertEquals(PasscodeResult.Rejected, results.last())
        assertEquals(1_000L, manager.remainingLockoutMillis())
        assertEquals(PasscodeStep.ChangeVerify, manager.state.value.passcodeStep)
    }

    @Test
    fun logoutClearsPersistedAttemptState() {
        val storage = TestStorage(passcode = PASSCODE)
        val manager = createManager(
            storage = storage,
            policy = PasscodeBruteForcePolicy(maxFailedAttempts = 3, lockoutDurations = listOf(1.seconds)),
            currentTimeMillis = { 1_000L },
        )

        enterPasscode(manager, WRONG_PASSCODE)
        assertEquals(1, storage.attemptState?.failedAttempts)

        manager.logOut()

        assertNull(storage.passcode)
        assertNull(storage.attemptState)
    }

    private fun createManager(
        storage: TestStorage,
        policy: PasscodeBruteForcePolicy,
        currentTimeMillis: () -> Long,
    ): PasscodeManager = PasscodeManager.createForTest(
        adapter = storage,
        attemptStorageAdapter = storage,
        bruteForcePolicy = policy,
        currentTimeMillis = currentTimeMillis,
    )

    private fun enterPasscode(manager: PasscodeManager, passcode: String) {
        passcode.forEach { manager.enterKey(it.toString()) }
    }

    private class TestStorage(
        var passcode: String?,
    ) : PasscodeStorageAdapter, PasscodeAttemptStorageAdapter {
        var attemptState: PasscodeAttemptState? = null

        override fun savePasscode(passcode: String) {
            this.passcode = passcode
        }

        override fun loadPasscode(): String? = passcode

        override fun deletePasscode() {
            passcode = null
        }

        override fun savePasscodeAttemptState(state: PasscodeAttemptState) {
            attemptState = state
        }

        override fun loadPasscodeAttemptState(): PasscodeAttemptState? = attemptState

        override fun deletePasscodeAttemptState() {
            attemptState = null
        }
    }

    private class LegacyStorage(
        var passcode: String?,
    ) : PasscodeStorageAdapter {
        override fun savePasscode(passcode: String) {
            this.passcode = passcode
        }

        override fun loadPasscode(): String? = passcode

        override fun deletePasscode() {
            passcode = null
        }
    }

    private companion object {
        const val PASSCODE = "1234"
        const val WRONG_PASSCODE = "9999"
    }
}
