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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mifos.authenticator.passcode.utility.PasscodeLength

/**
 * Remembers and provides a [PasscodeManager] instance within a Compose composition.
 *
 * This function handles the lifecycle of the [PasscodeManager], ensuring it is
 * properly initialized and remembered across recompositions.
 *
 * @param adapter The [PasscodeStorageAdapter] to use for persisting and loading passcodes.
 * @param scope The [CoroutineScope] to launch coroutines for handling passcode actions and events.
 * @return An initialized [PasscodeManager] instance.
 */
@Composable
fun rememberPasscodeManager(
    adapter: PasscodeStorageAdapter,
    scope: CoroutineScope,
): PasscodeManager {
    return remember(scope) {
        PasscodeManager(adapter, scope).initialize()
    }
}

/**
 * Manages the state and logic for passcode creation, entry, and validation.
 *
 * This class orchestrates the interaction between the UI (via [PasscodeAction]s and [PasscodeEvent]s)
 * and the underlying storage ([PasscodeStorageAdapter]) for passcodes. It maintains the current
 * state of the passcode entry process in a [StateFlow].
 *
 * @param adapter The [PasscodeStorageAdapter] used for persisting and loading passcodes.
 * @param scope The [CoroutineScope] within which all internal coroutines (for handling actions and events) are launched.
 */
class PasscodeManager(
    private val adapter: PasscodeStorageAdapter,
    private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow(
        PasscodeState(
            loadedPasscode = adapter.loadPasscode(),
        ),
    )

    /**
     * A [StateFlow] representing the current state of the passcode UI.
     * Collect this flow to react to changes in the passcode entry process.
     */
    val state = _state.asStateFlow()

    private val _events = Channel<PasscodeEvent>(capacity = Channel.UNLIMITED)

    /**
     * A [Flow] of [PasscodeEvent]s that can be collected to receive one-time events
     * such as successful unlock, creation, or rejection of passcodes.
     */
    val events = _events.receiveAsFlow()

    private val _actions = Channel<PasscodeAction>(capacity = Channel.UNLIMITED)

    /**
     * A [SendChannel] for [PasscodeAction]s. Send actions to this channel to trigger
     * state changes and logic within the [PasscodeManager].
     */
    val actions: SendChannel<PasscodeAction> = _actions

    init {
        scope.launch {
            _actions.consumeAsFlow().collect { action ->
                handleAction(action)
            }
        }
    }

    private val creationPasscodeBuilder = StringBuilder()
    private val finalConfirmationPasscodeBuilder = StringBuilder()

    /**
     * Initializes the [PasscodeManager] by loading any existing passcode and setting
     * the initial [PasscodeStep].
     *
     * @return The initialized [PasscodeManager] instance.
     */
    fun initialize(): PasscodeManager {
        clearStates()
        val loaded = adapter.loadPasscode()
        if (loaded != null) {
            updateState {
                it.copy(
                    loadedPasscode = loaded,
                    passcodeStep = PasscodeStep.Enter,
                )
            }
        } else {
            updateState {
                it.copy(passcodeStep = PasscodeStep.Create)
            }
        }
        loaded?.let {
            updatePasscodeLength(
                when (it.length) {
                    6 -> PasscodeLength.SIX_DIGIT
                    else -> PasscodeLength.FOUR_DIGIT
                },
            )
        }
        return this
    }

    /**
     * Handles incoming [PasscodeAction]s and updates the internal state accordingly.
     *
     * @param action The [PasscodeAction] to be processed.
     */
    fun handleAction(action: PasscodeAction) {
        when (action) {
            PasscodeAction.ChangePasscode -> changePasscode()
            PasscodeAction.DeleteAllKeys -> deleteAllKeys()
            PasscodeAction.DeleteKey -> deleteKey()
            is PasscodeAction.EnterKey -> enterKey(action.key)
            PasscodeAction.ForgetPasscode -> deletePasscode()
            PasscodeAction.TogglePasscodeVisibility -> togglePasscodeVisibility()
            is PasscodeAction.UpdatePasscodeLength -> updatePasscodeLength(action.length)
            PasscodeAction.LogOutErasePasscode -> {
                adapter.deletePasscode()
            }
        }
    }

    private fun updatePasscodeLength(length: PasscodeLength) {
        updateState {
            it.copy(
                passcodeLength = length,
            )
        }
    }

    private fun deleteKey() {
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

    private fun deleteAllKeys() {
        getActivePasscodeBuilder().clear()
        updateState { it.copy(currentPasscodeInput = "", filledDots = 0) }
    }

    private fun enterKey(key: String) {
        val currentState = _state.value

        if (currentState.filledDots >= _state.value.passcodeLength.length) {
            return
        }

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

    private fun togglePasscodeVisibility() {
        updateState {
            it.copy(
                passcodeVisible = !_state.value.passcodeVisible,
            )
        }
    }

    private fun changePasscode() {
        updateState {
            it.copy(passcodeStep = PasscodeStep.ChangeVerify)
        }
    }

    private fun handleCompletedPasscodeEntry() {
        when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify -> handleChangeVerifyPasscode()
            PasscodeStep.Confirm -> handleConfirmPasscode()
            PasscodeStep.Create -> handleCreatePasscode()
            PasscodeStep.Enter -> handleEnterPasscode()
            else -> {}
        }
    }

    private fun getActivePasscodeBuilder(): StringBuilder {
        return when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify,
            PasscodeStep.Confirm,
            PasscodeStep.Enter,
            -> {
                finalConfirmationPasscodeBuilder
            }
            else -> {
                creationPasscodeBuilder
            }
        }
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
                )
            }
        } else {
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
        clearStates()
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
            updateState {
                it.copy(
                    currentPasscodeInput = "",
                    filledDots = 0,
                    passcodeVisible = false,
                    passcodeStep = PasscodeStep.Enter,
                    loadedPasscode = newPasscode,
                )
            }
            creationPasscodeBuilder.clear()
            emitEvent(PasscodeEvent.OnCreateSuccess)
        } else {
            updateState {
                it.copy(
                    currentPasscodeInput = "",
                    filledDots = 0,
                )
            }
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
        finalConfirmationPasscodeBuilder.clear()
    }

    private fun handleEnterPasscode() {
        if (finalConfirmationPasscodeBuilder.toString() == _state.value.loadedPasscode) {
            emitEvent(PasscodeEvent.OnUnlockSuccess)
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
        }
        clearStates()
    }

    private fun clearStates() {
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

    private fun deletePasscode() {
        adapter.deletePasscode()
        updateState { it.copy(loadedPasscode = null, passcodeStep = PasscodeStep.Create) }
        clearStates()
        emitEvent(PasscodeEvent.OnPasscodeDeletion)
    }

    private fun updateState(update: (PasscodeState) -> PasscodeState) {
        _state.update {
            update(it)
        }
    }

    private fun emitEvent(event: PasscodeEvent) = scope.launch {
        _events.send(event)
    }

    /**
     * Sends a [PasscodeAction] to the manager. This function is deprecated in favor of `trySendAction`.
     *
     * @param action The [PasscodeAction] to send.
     */
    private fun sendAction(action: PasscodeAction) {
        scope.launch {
            actions.send(action)
        }
    }

    /**
     * Attempts to send a [PasscodeAction] to the manager immediately.
     * This is the preferred method for sending actions from the UI.
     *
     * @param action The [PasscodeAction] to send.
     */
    fun trySendAction(action: PasscodeAction) {
        actions.trySend(action)
    }
}

/**
 * Represents the current UI state of the passcode screen.
 *
 * @property filledDots The number of filled dots in the passcode input (visual representation).
 * @property passcodeLength The currently selected [PasscodeLength] (e.g., 4-digit or 6-digit).
 * @property passcodeVisible Whether the entered passcode characters are visible or masked.
 * @property currentPasscodeInput The actual passcode characters entered so far.
 * @property loadedPasscode The passcode loaded from storage, if any.
 * @property passcodeStep The current [PasscodeStep] in the passcode flow (e.g., Create, Enter, Confirm).
 */
data class PasscodeState(
    val filledDots: Int = 0,
    val passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val loadedPasscode: String? = null,
    val passcodeStep: PasscodeStep = PasscodeStep.Unset,
)

/**
 * Represents one-time events emitted by the [PasscodeManager] that the UI can react to.
 */
sealed interface PasscodeEvent {
    /** Indicates that the passcode was successfully entered and unlocked. */
    object OnUnlockSuccess : PasscodeEvent

    /** Indicates that a new passcode was successfully created. */
    object OnCreateSuccess : PasscodeEvent

    /** Indicates that the entered passcode for unlocking was incorrect. */
    object OnRejectEnteredPasscode : PasscodeEvent

    /** Indicates that the confirmation passcode did not match the initial creation passcode. */
    object OnRejectConfirmationPasscode : PasscodeEvent

    /** Indicates that the passcode was successfully deleted. */
    object OnPasscodeDeletion : PasscodeEvent
}

/**
 * Represents actions that can be performed on the [PasscodeManager] to change its state or trigger logic.
 */
sealed interface PasscodeAction {
    /** Action to delete the stored passcode. */
    object ForgetPasscode : PasscodeAction
    object LogOutErasePasscode : PasscodeAction

    /** Action to initiate the passcode change flow. */
    object ChangePasscode : PasscodeAction

    /** Action to toggle the visibility of the entered passcode characters. */
    object TogglePasscodeVisibility : PasscodeAction

    /** Action to delete the last entered character from the passcode input. */
    object DeleteKey : PasscodeAction

    /** Action to clear all entered characters from the passcode input. */
    object DeleteAllKeys : PasscodeAction

    /**
     * Action to enter a single digit key into the passcode input.
     * @param key The digit (as a String) to enter.
     */
    data class EnterKey(val key: String) : PasscodeAction

    /**
     * Action to update the desired passcode length.
     * @param length The new [PasscodeLength] to set.
     */
    data class UpdatePasscodeLength(val length: PasscodeLength) : PasscodeAction
}

/**
 * Defines the various steps in the passcode interaction flow.
 *
 * @property index An integer representing the order or stage of the step, useful for progress indicators.
 */
enum class PasscodeStep(val index: Int) {
    /** Initial or undefined state. */
    Unset(-1),

    /** Step where the user is prompted to enter an existing passcode. */
    Enter(-1),

    /** Step where the user is prompted to create a new passcode. */
    Create(1),

    /** Step where the user is prompted to confirm the newly created passcode. */
    Confirm(2),

    /** Step where the user needs to verify their current passcode before changing it. */
    ChangeVerify(0),
}
