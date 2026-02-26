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
 * The manager follows a unidirectional data flow (UDF) pattern:
 * - **Actions**: UI sends [PasscodeAction]s to trigger logic.
 * - **State**: UI collects [state] to reflect the current UI configuration.
 * - **Events**: UI collects [events] for one-time side effects (e.g., navigation on success).
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

    /** Temporary buffer for the passcode being created in the [PasscodeStep.Create] step. */
    private val creationPasscodeBuilder = StringBuilder()

    /** Temporary buffer for the passcode being verified or confirmed in other steps. */
    private val finalConfirmationPasscodeBuilder = StringBuilder()

    /**
     * Initializes the [PasscodeManager] by loading any existing passcode and setting
     * the initial [PasscodeStep].
     *
     * If a passcode is already stored, it sets the step to [PasscodeStep.Enter].
     * Otherwise, it sets the step to [PasscodeStep.Create].
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
            PasscodeAction.SkipPasscodeCreation -> {
                updateState {
                    it.copy(passcodeStep = PasscodeStep.Skipped)
                }
                emitEvent(PasscodeEvent.OnPasscodeSkip)
            }
        }
    }

    /**
     * Updates the expected passcode length in the state.
     */
    private fun updatePasscodeLength(length: PasscodeLength) {
        updateState {
            it.copy(
                passcodeLength = length,
            )
        }
    }

    /**
     * Deletes the last entered character from the active passcode builder.
     */
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

    /**
     * Clears all characters from the active passcode builder.
     */
    private fun deleteAllKeys() {
        getActivePasscodeBuilder().clear()
        updateState { it.copy(currentPasscodeInput = "", filledDots = 0) }
    }

    /**
     * appends a character to the active passcode builder if the limit hasn't been reached.
     * Triggers completion logic if the builder reaches the required length.
     */
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

    /**
     * Toggles the visibility state of the passcode in the UI.
     */
    private fun togglePasscodeVisibility() {
        updateState {
            it.copy(
                passcodeVisible = !_state.value.passcodeVisible,
            )
        }
    }

    /**
     * Transitions the step to [PasscodeStep.ChangeVerify] to start the passcode change flow.
     */
    private fun changePasscode() {
        updateState {
            it.copy(passcodeStep = PasscodeStep.ChangeVerify)
        }
    }

    /**
     * Dispatches the logic for handling a full passcode entry based on the current [PasscodeStep].
     */
    private fun handleCompletedPasscodeEntry() {
        when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify -> handleChangeVerifyPasscode()
            PasscodeStep.Confirm -> handleConfirmPasscode()
            PasscodeStep.Create -> handleCreatePasscode()
            PasscodeStep.Enter -> handleEnterPasscode()
            else -> {}
        }
    }

    /**
     * Determines which [StringBuilder] to use based on the current flow step.
     */
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

    /**
     * Validates the entered passcode against the stored one during the change flow.
     * Transitions to [PasscodeStep.Create] on success, or emits [PasscodeEvent.OnRejectConfirmationPasscode] on failure.
     */
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

    /**
     * Transitions from [PasscodeStep.Create] to [PasscodeStep.Confirm] after the first entry.
     */
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

    /**
     * Compares the confirmation entry with the initial creation entry.
     * Saves the passcode on success, otherwise emits a rejection event.
     */
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

    /**
     * Validates the entered passcode against the stored one to unlock.
     */
    private fun handleEnterPasscode() {
        if (finalConfirmationPasscodeBuilder.toString() == _state.value.loadedPasscode) {
            emitEvent(PasscodeEvent.OnUnlockSuccess)
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
        }
        clearStates()
    }

    /**
     * Resets the input-related state and clears the temporary builders.
     */
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

    /**
     * Deletes the passcode from storage and resets the manager to the creation flow.
     */
    private fun deletePasscode() {
        adapter.deletePasscode()
        updateState { it.copy(loadedPasscode = null, passcodeStep = PasscodeStep.Create) }
        clearStates()
        emitEvent(PasscodeEvent.OnPasscodeDeletion)
    }

    /**
     * Utility to update the [_state] flow in a thread-safe manner.
     */
    private fun updateState(update: (PasscodeState) -> PasscodeState) {
        _state.update {
            update(it)
        }
    }

    /**
     * Sends an event to the [_events] channel.
     */
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

    object OnPasscodeSkip : PasscodeEvent
}

/**
 * Represents actions that can be performed on the [PasscodeManager] to change its state or trigger logic.
 */
sealed interface PasscodeAction {
    /**
     * Action for the "Forgot Passcode?" flow inside [PasscodeScreen].
     *
     * Deletes the stored passcode, resets the manager state to [PasscodeStep.Create],
     * and emits [PasscodeEvent.OnPasscodeDeletion] so the screen can navigate away.
     * **Only dispatch this action when [PasscodeScreen] is active** (i.e. when there is an active
     * collector on [PasscodeManager.events]). Dispatching it while [PasscodeScreen] is not in the
     * back stack will buffer the event; it will then fire immediately the next time the screen
     * is opened, sending the user back to login before they can interact.
     */
    object ForgetPasscode : PasscodeAction

    /**
     * Action to erase the stored passcode during a logout flow from outside [PasscodeScreen].
     *
     * Calls the [PasscodeStorageAdapter] to delete the passcode directly without emitting any
     * event. Use this instead of [ForgetPasscode] whenever [PasscodeScreen] is not currently
     * in the back stack (e.g. a logout button on a Home or Settings screen).
     */
    object LogOutErasePasscode : PasscodeAction
    object SkipPasscodeCreation : PasscodeAction

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

    Skipped(3),
}
