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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.mifos.authenticator.passcode.utility.PasscodeLength
import org.mifos.authenticator.passcode.utility.Step

/**
 * Composable function that creates and remembers a mifos.authenticator.passcode.PasscodeSaver instance
 *
 * @param currentPasscode The current saved passcode, if any
 * @param currentPasscodeLength The current saved passcode length, if any
 * @param isPasscodeSet Whether a passcode is already set
 * @param saveNewPasscode Function to save a new passcode
 * @param savePasscodeLength Function to save a new passcode length.
 * @param clearCurrentPasscode Function to clear the saved passcode
 * @return A mifos.authenticator.passcode.PasscodeSaver instance
 */
@Composable
fun rememberPasscodeSaver(
    currentPasscode: String,
    currentPasscodeLength: PasscodeLength,
    isPasscodeSet: Boolean,
    saveNewPasscode: (String) -> Unit,
    savePasscodeLength: (PasscodeLength) -> Unit,
    clearCurrentPasscode: () -> Unit,
): PasscodeSaver {
    val scope = rememberCoroutineScope()

    return remember(
        key1 = currentPasscode,
        key2 = isPasscodeSet,
    ) {
        PasscodeSaver(
            currentPasscode = currentPasscode,
            currentPasscodeLength = currentPasscodeLength,
            isPasscodeSet = isPasscodeSet,
            saveNewPasscode = saveNewPasscode,
            savePasscodeLength = savePasscodeLength,
            clearCurrentPasscode = clearCurrentPasscode,
            scope = scope,
        )
    }
}

/**
 * A class that manages passcode creation, confirmation, and validation
 */
class PasscodeSaver(
    private val currentPasscode: String,
    private val currentPasscodeLength: PasscodeLength,
    isPasscodeSet: Boolean,
    private val saveNewPasscode: (passcode: String) -> Unit,
    private val savePasscodeLength: (PasscodeLength) -> Unit,
    private val clearCurrentPasscode: () -> Unit,
    private val scope: CoroutineScope,
) {
    // Events
    private val _events = Channel<PasscodeEvents>()
    val events = _events.receiveAsFlow()

    // State
    private val _state = MutableStateFlow(
        PasscodeStates(
            isPasscodeAlreadySet = isPasscodeSet,
            passcodeLength = currentPasscodeLength,
        ),
    )
    val state: StateFlow<PasscodeStates> = _state.asStateFlow()

    var attempts = 0

    // Internal data
    private var createPasscode = StringBuilder()
    private var confirmPasscode = StringBuilder()

    init {
        restart()
    }

    /**
     * Updates the state with new values
     */
    private fun updateState(update: PasscodeStates.() -> PasscodeStates) {
        _state.value = _state.value.update()
    }

    /**
     * Emits a passcode event
     */
    private fun emitEvent(event: PasscodeEvents) = scope.launch {
        _events.trySend(event)
    }

    /**
     * Gets the active passcode builder based on current step
     */
    private fun getActivePasscodeBuilder(): StringBuilder {
        return when (_state.value.activeStep) {
            Step.Create, Step.Enter, Step.Change -> createPasscode
            Step.Confirm -> confirmPasscode
        }
    }

    /**
     * Handles logic when a passcode entry is completed
     */
    private fun handleCompletedPasscodeEntry() {
        val currentState = _state.value

        when {
            // Validating an existing passcode
            currentState.isPasscodeAlreadySet -> {
                if (currentPasscode == createPasscode.toString()) {
                    if (currentState.activeStep == Step.Change) {
                        updateState {
                            copy(
                                activeStep = Step.Create,
                                filledDots = 0,
                                currentPasscodeInput = "",
                                isPasscodeAlreadySet = false,
                            )
                        }
                    } else {
                        emitEvent(
                            PasscodeEvents.PasscodeConfirmed(
                                currentPasscode,
                            ),
                        )
                    }
                    createPasscode.clear()
                } else {
                    emitEvent(PasscodeEvents.PasscodeRejected)
                    attempts++
                    updateState {
                        copy(
                            attempts = this@PasscodeSaver.attempts,
                        )
                    }
                    // Logic for retries can be written here
                }
                updateState { copy(currentPasscodeInput = "") }
            }

            // Creating a new passcode
            currentState.activeStep == Step.Create -> {
                updateState {
                    copy(
                        activeStep = Step.Confirm,
                        filledDots = 0,
                        currentPasscodeInput = "",
                    )
                }
            }

            // Confirming a new passcode
            else -> {
                if (createPasscode.toString() == confirmPasscode.toString()) {
                    val confirmedPasscode = confirmPasscode.toString()
                    emitEvent(
                        PasscodeEvents.PasscodeConfirmed(
                            confirmedPasscode,
                        ),
                    )
                    saveNewPasscode(confirmedPasscode)
                    savePasscodeLength(_state.value.passcodeLength)
                    updateState { copy(isPasscodeAlreadySet = true) }
                    restart()
                } else {
                    emitEvent(PasscodeEvents.PasscodeRejected)
                    restart()
                }
            }
        }
    }

    /**
     * Toggles passcode visibility
     */
    fun togglePasscodeVisibility() {
        updateState { copy(passcodeVisible = !passcodeVisible) }
    }

    /**
     * Changes passcode length. If switch is on then 6 and 4 if off, which is the default length.
     */
    fun updatePasscodeLength(length: PasscodeLength) {
        updateState {
            copy(
                passcodeLength = length,
            )
        }
    }

    /**
     * Restarts the passcode entry process
     */
    fun restart() {
        createPasscode.clear()
        confirmPasscode.clear()
        updateState {
            copy(
                filledDots = 0,
                currentPasscodeInput = "",
                activeStep = if (isPasscodeAlreadySet && currentPasscode.isNotEmpty()) {
                    Step.Enter
                } else {
                    Step.Create
                },
            )
        }
    }

    /**
     * Processes a key press during passcode entry
     *
     * @param key The key that was pressed
     */
    fun enterKey(key: String) {
        val currentState = _state.value

        // Don't process input if we've reached the passcode length
        if (currentState.filledDots >= _state.value.passcodeLength.length) {
            return
        }

        // Get the appropriate passcode builder based on current step
        val passcodeBuilder = getActivePasscodeBuilder()

        // Append the key to the passcode
        passcodeBuilder.append(key)

        // Update the state with the new input
        updateState {
            copy(
                currentPasscodeInput = passcodeBuilder.toString(),
                filledDots = passcodeBuilder.length,
            )
        }

        // Handle completed passcode entry
        if (passcodeBuilder.length == _state.value.passcodeLength.length) {
            handleCompletedPasscodeEntry()
        }
    }

    /**
     * Deletes the last entered key
     */
    fun deleteKey() {
        val passcodeBuilder = getActivePasscodeBuilder()

        if (passcodeBuilder.isNotEmpty()) {
            passcodeBuilder.deleteAt(passcodeBuilder.length - 1)
            updateState {
                copy(
                    currentPasscodeInput = passcodeBuilder.toString(),
                    filledDots = passcodeBuilder.length,
                )
            }
        }
    }

    /**
     * Clears all entered keys
     */
    fun deleteAllKeys() {
        getActivePasscodeBuilder().clear()
        updateState { copy(currentPasscodeInput = "", filledDots = 0) }
    }

    /**
     * Forgets the saved passcode
     */
    fun forgetPasscode() {
        // Delete passcode from database
        clearCurrentPasscode()
        updateState {
            copy(
                isPasscodeAlreadySet = false,
                passcodeVisible = false,
            )
        }
        restart()
    }

    fun resetPasscode() {
        updateState {
            copy(
                activeStep = Step.Change,
                filledDots = 0,
            )
        }
    }

}


/**
 * A data class representing passcode UI state
 */
data class PasscodeStates(
    val activeStep: Step = Step.Create,
    val filledDots: Int = 0,
    val passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val isPasscodeAlreadySet: Boolean = false,
    val attempts: Int = 0,
)



/**
 * A sealed interface representing passcode events
 */
sealed interface PasscodeEvents {
    data class PasscodeConfirmed(val passcode: String) : PasscodeEvents
    data object PasscodeRejected : PasscodeEvents

    data object NoPasscodeAction : PasscodeEvents
}
