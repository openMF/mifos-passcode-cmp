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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mifos.authenticator.passcode.screen.PasscodeScreen
import org.mifos.authenticator.passcode.utility.PasscodeLength

/**
 * Manages the state and logic for passcode creation, entry, and validation.
 *
 * It handles various flows such as:
 * - Initial passcode setup (Creation and Confirmation)
 * - Passcode verification for app unlock
 * - Changing the existing passcode
 * - Disabling external authentication (e.g. biometrics) with passcode verification
 *
 * Results are delivered via a [PasscodeResult] callback registered by [PasscodeScreen].
 * Use [onResult] in [PasscodeScreen] to handle navigation and other outcomes.
 *
 * This class should be scoped as a singleton via your DI framework (Koin, Hilt, etc.)
 * since it is used across multiple screens.
 *
 * @param adapter The [PasscodeStorageAdapter] used for persisting and loading passcodes.
 */

class PasscodeManager(
    private val adapter: PasscodeStorageAdapter,
) {
    private val _state = MutableStateFlow(
        PasscodeState(
            loadedPasscode = adapter.loadPasscode(),
        ),
    )

    /**
     * The current state of the passcode system, exposed as a read-only [StateFlow].
     */
    val state = _state.asStateFlow()

    private var onResult: ((PasscodeResult) -> Unit)? = null

    private val creationPasscodeBuilder = StringBuilder()
    private val finalConfirmationPasscodeBuilder = StringBuilder()

    init {
        val loaded = adapter.loadPasscode()

        when {
            loaded != null -> {
                updateState {
                    it.copy(
                        loadedPasscode = loaded,
                        passcodeStep = PasscodeStep.Enter,
                    )
                }
                updatePasscodeLength(
                    if (loaded.length == 6) {
                        PasscodeLength.SIX_DIGIT
                    } else {
                        PasscodeLength.FOUR_DIGIT
                    },
                )
            }

            else -> {
                updateState {
                    it.copy(
                        passcodeStep = PasscodeStep.Create,
                    )
                }
            }
        }
    }

    /**
     * Initiates the change passcode flow. Sets the step to [PasscodeStep.ChangeVerify],
     * requiring the user to verify their current passcode before creating a new one.
     *
     * After successful verification and confirmation, [PasscodeResult.Changed] is emitted.
     */
    fun changePasscode() {
        updateState { it.copy(passcodeStep = PasscodeStep.ChangeVerify, isChangeFlow = true) }
    }

    /**
     * Clears the stored passcode and resets the manager to the creation state.
     * No [PasscodeResult] is emitted — the caller handles navigation directly.
     *
     * Use this for logout flows from screens other than [PasscodeScreen].
     */
    fun logOut() {
        clearAllSecurityData()
    }

    /**
     * Notifies the manager that external authentication (e.g. biometrics) succeeded.
     * Emits [PasscodeResult.Verified], bypassing passcode entry.
     *
     * Call this from your external auth button when authentication is successful.
     */
    fun notifyExternalAuthSuccess() {
        emitResult(PasscodeResult.Verified)
    }

    /**
     * Registers a callback to receive [PasscodeResult]s.
     * Called by [PasscodeScreen] via [DisposableEffect] — not intended for consumer use.
     */
    internal fun setResultCallback(callback: ((PasscodeResult) -> Unit)?) {
        onResult = callback
    }

    /**
     * Clears the stored passcode and emits [PasscodeResult.Forgotten].
     * Called by the "Forgot Passcode?" button inside [PasscodeScreen].
     */
    internal fun forgetPasscode() {
        clearAllSecurityData()
        emitResult(PasscodeResult.Forgotten)
    }

    internal fun updatePasscodeLength(length: PasscodeLength) {
        updateState { it.copy(passcodeLength = length) }
    }

    internal fun deleteKey() {
        val passcodeBuilder = getActivePasscodeBuilder()
        if (passcodeBuilder.isNotEmpty()) {
            passcodeBuilder.deleteAt(passcodeBuilder.length - 1)
            updateState {
                it.copy(
                    currentPasscodeInput = passcodeBuilder.toString(),
                    filledDots = passcodeBuilder.length,
                )
            }
        }
    }

    internal fun deleteAllKeys() {
        getActivePasscodeBuilder().clear()
        updateState { it.copy(currentPasscodeInput = "", filledDots = 0) }
    }

    internal fun enterKey(key: String) {
        val currentState = _state.value
        if (currentState.filledDots >= _state.value.passcodeLength.length) return

        val passcodeBuilder = getActivePasscodeBuilder()
        passcodeBuilder.append(key)

        updateState {
            it.copy(
                currentPasscodeInput = passcodeBuilder.toString(),
                filledDots = passcodeBuilder.length,
            )
        }

        if (passcodeBuilder.length == _state.value.passcodeLength.length) {
            handleCompletedPasscodeEntry()
        }
    }

    internal fun togglePasscodeVisibility() {
        updateState { it.copy(passcodeVisible = !_state.value.passcodeVisible) }
    }

    private fun handleCompletedPasscodeEntry() {
        when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify -> handleChangeVerifyPasscode()
            PasscodeStep.Enter -> handleEnterPasscode()
            PasscodeStep.Create -> handleCreatePasscode()
            PasscodeStep.Confirm -> handleConfirmPasscode()
            else -> {}
        }
    }

    private fun getActivePasscodeBuilder(): StringBuilder {
        return when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify,
            PasscodeStep.Confirm,
            PasscodeStep.Enter,
            -> finalConfirmationPasscodeBuilder
            else -> creationPasscodeBuilder
        }
    }

    private fun emitResult(result: PasscodeResult) {
        onResult?.invoke(result)
    }

    private fun handleChangeVerifyPasscode() {
        val loadedPasscode = adapter.loadPasscode()
        if (finalConfirmationPasscodeBuilder.toString() == loadedPasscode) {
            updateState {
                it.copy(
                    passcodeStep = PasscodeStep.Create,
                    passcodeLength = when (loadedPasscode.length) {
                        6 -> PasscodeLength.SIX_DIGIT
                        else -> PasscodeLength.FOUR_DIGIT
                    },
                    isChangeFlow = true,
                )
            }
        } else {
            updateState {
                it.copy(shakeAnimationTrigger = it.shakeAnimationTrigger + 1)
            }
            emitResult(PasscodeResult.Rejected)
        }
        resetPasscodeEntryStates()
    }

    private fun handleCreatePasscode() {
        updateState {
            it.copy(
                currentPasscodeInput = "",
                filledDots = 0,
                passcodeVisible = false,
                passcodeStep = PasscodeStep.Confirm,
            )
        }
    }

    private fun handleConfirmPasscode() {
        val newPasscode = finalConfirmationPasscodeBuilder.toString()
        if (creationPasscodeBuilder.toString() == newPasscode) {
            adapter.savePasscode(newPasscode)
            val isChange = _state.value.isChangeFlow
            updateState {
                it.copy(
                    currentPasscodeInput = "",
                    filledDots = 0,
                    passcodeVisible = false,
                    passcodeStep = PasscodeStep.Enter,
                    loadedPasscode = newPasscode,
                    isChangeFlow = false,
                )
            }
            creationPasscodeBuilder.clear()
            if (isChange) {
                emitResult(PasscodeResult.Changed)
            } else {
                emitResult(PasscodeResult.Created)
            }
        } else {
            updateState { it.copy(currentPasscodeInput = "", filledDots = 0) }

            updateState {
                it.copy(shakeAnimationTrigger = it.shakeAnimationTrigger + 1)
            }
        }
        resetPasscodeEntryStates()
    }

    private fun handleEnterPasscode() {
        if (finalConfirmationPasscodeBuilder.toString() == _state.value.loadedPasscode) {
            emitResult(PasscodeResult.Verified)
        } else {
            emitResult(PasscodeResult.Rejected)
            updateState {
                it.copy(shakeAnimationTrigger = it.shakeAnimationTrigger + 1)
            }
        }
        resetPasscodeEntryStates()
    }

    private fun resetPasscodeEntryStates() {
        updateState {
            it.copy(
                filledDots = 0,
                currentPasscodeInput = "",
                passcodeVisible = false,
                passcodeLength = when (_state.value.loadedPasscode?.length) {
                    6 -> PasscodeLength.SIX_DIGIT
                    else -> PasscodeLength.FOUR_DIGIT
                },
            )
        }
        finalConfirmationPasscodeBuilder.clear()
        creationPasscodeBuilder.clear()
    }

    private fun clearAllSecurityData() {
        adapter.deletePasscode()
        updateState {
            it.copy(
                loadedPasscode = null,
                passcodeStep = PasscodeStep.Create,
                isChangeFlow = false,
            )
        }
        resetPasscodeEntryStates()
    }

    private fun updateState(update: (PasscodeState) -> PasscodeState) {
        _state.update { update(it) }
    }
}

/**
 * Represents the UI state of the passcode screen.
 *
 * @property filledDots The number of digits currently entered by the user.
 * @property passcodeLength The required length of the passcode (4 or 6 digits).
 * @property passcodeVisible Whether the entered passcode is visually masked or visible.
 * @property currentPasscodeInput The current string representing the entered passcode.
 * @property loadedPasscode The saved passcode from storage (null if not set).
 * @property passcodeStep The current step in the passcode flow (e.g., Enter, Create, Confirm).
 * @property isChangeFlow Whether the user is currently in the process of changing their passcode.
 */
data class PasscodeState(
    val filledDots: Int = 0,
    val passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val loadedPasscode: String? = null,
    val passcodeStep: PasscodeStep = PasscodeStep.Unset,
    val isChangeFlow: Boolean = false,
    val shakeAnimationTrigger: Int = 0,
)

/**
 * Results emitted by [PasscodeManager] to indicate the outcome of a passcode operation.
 * Delivered via the `onResult` callback in [PasscodeScreen].
 */
sealed interface PasscodeResult {
    /** Passcode entered correctly or external authentication succeeded. */
    data object Verified : PasscodeResult

    /** New passcode created and confirmed for the first time. */
    data object Created : PasscodeResult

    /** Existing passcode changed successfully. */
    data object Changed : PasscodeResult

    /** Passcode deleted via "Forgot Passcode?" button. */
    data object Forgotten : PasscodeResult

    /** Incorrect passcode entered. */
    data object Rejected : PasscodeResult
}

/**
 * Represents the current step in the passcode interaction lifecycle.
 */
enum class PasscodeStep {
    /** Initial state, no step set. */
    Unset,

    /** User is entering their passcode to unlock the app. */
    Enter,

    /** User is creating a new passcode for the first time. */
    Create,

    /** User is confirming the newly created passcode. */
    Confirm,

    /** User is verifying their old passcode before changing it. */
    ChangeVerify,
}
