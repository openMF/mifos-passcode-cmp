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
 * It handles various flows such as:
 * - Initial passcode setup (Creation and Confirmation)
 * - Passcode verification for app unlock
 * - Changing the existing passcode
 * - Enabling/Disabling biometric authentication
 *
 * @property adapter The storage adapter used for persisting passcode and biometric data.
 * @property scope Coroutine scope for internal processing and event emission.
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
     * The current state of the passcode system, exposed as a read-only [StateFlow].
     */
    val state = _state.asStateFlow()

    private val _events = Channel<PasscodeEvent>(capacity = Channel.UNLIMITED)

    /**
     * A flow of [PasscodeEvent]s emitted by the manager (e.g., success, failure, deletion).
     */
    val events = _events.receiveAsFlow()

    private val _actions = Channel<PasscodeAction>(capacity = Channel.UNLIMITED)

    /**
     * A [SendChannel] to send [PasscodeAction]s to the manager.
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
     * Initializes the [PasscodeManager] by loading existing data and setting the initial step.
     *
     * Should be called once after creation, especially when not using [rememberPasscodeManager].
     *
     * @return The initialized [PasscodeManager] instance.
     */
    fun initialize(): PasscodeManager {
        val loaded = adapter.loadPasscode()
        val biometricRegistered = adapter.loadRegistrationData() != null

        when {
            loaded != null -> {
                updateState {
                    it.copy(
                        loadedPasscode = loaded,
                        passcodeStep = PasscodeStep.Enter,
                        isBiometricEnabled = biometricRegistered,
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
                        isBiometricEnabled = biometricRegistered,
                    )
                }
            }
        }
        return this
    }


    private fun handleAction(action: PasscodeAction) {
        when (action) {
            PasscodeAction.ChangePasscode -> changePasscode()
            PasscodeAction.DisableBiometrics -> disableBiometrics()
            PasscodeAction.DeleteAllKeys -> deleteAllKeys()
            PasscodeAction.DeleteKey -> deleteKey()
            is PasscodeAction.EnterKey -> enterKey(action.key)
            PasscodeAction.ForgetPasscode -> {
                dataArmageddon()
                emitEvent(PasscodeEvent.OnPasscodeDeletion)
            }
            PasscodeAction.TogglePasscodeVisibility -> togglePasscodeVisibility()
            is PasscodeAction.UpdatePasscodeLength -> updatePasscodeLength(action.length)
            PasscodeAction.LogOutErase -> {
                dataArmageddon()
            }
            PasscodeAction.BiometricUnlockSuccess -> {
                if (_state.value.passcodeStep == PasscodeStep.Enter) {
                    emitEvent(PasscodeEvent.OnUnlockSuccess)
                }
            }
            is PasscodeAction.BiometricUnlockFailure -> {
                if (_state.value.passcodeStep == PasscodeStep.Enter) {
                    emitEvent(PasscodeEvent.OnBiometricUnlockFailure(action.message))
                }
            }
            PasscodeAction.BiometricUserNotRegistered -> {
                biometricsDataArmageddon()
                if (_state.value.passcodeStep == PasscodeStep.Enter) {
                    emitEvent(PasscodeEvent.OnBiometricUserNotRegistered)
                }
            }
            is PasscodeAction.SaveBiometricRegistration -> {
                adapter.saveRegistrationData(action.registrationData)
                updateState { it.copy(isBiometricEnabled = true) }
            }
            PasscodeAction.DeleteBiometricRegistration -> biometricsDataArmageddon()
        }
    }

    private fun updatePasscodeLength(length: PasscodeLength) {
        updateState { it.copy(passcodeLength = length) }
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

    private fun togglePasscodeVisibility() {
        updateState { it.copy(passcodeVisible = !_state.value.passcodeVisible) }
    }

    private fun changePasscode() {
        updateState { it.copy(passcodeStep = PasscodeStep.ChangeVerify, isChangeFlow = true) }
    }

    private fun disableBiometrics() {
        updateState { it.copy(passcodeStep = PasscodeStep.DisableBiometrics) }
    }

    private fun handleCompletedPasscodeEntry() {
        when (_state.value.passcodeStep) {
            PasscodeStep.ChangeVerify -> handleChangeVerifyPasscode()
            PasscodeStep.DisableBiometrics -> handleDisableBiometricsVerification()
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
            PasscodeStep.DisableBiometrics,
            -> finalConfirmationPasscodeBuilder
            else -> creationPasscodeBuilder
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
                    isChangeFlow = true,
                )
            }
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
        }
        resetPasscodeEntryStates()
    }

    private fun handleDisableBiometricsVerification() {
        val loadedPasscode = adapter.loadPasscode()
        if (finalConfirmationPasscodeBuilder.toString() == loadedPasscode) {
            biometricsDataArmageddon()
            emitEvent(PasscodeEvent.OnDisableBiometricsSuccess)
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
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
                emitEvent(PasscodeEvent.OnPasscodeChanged)
            } else {
                emitEvent(PasscodeEvent.OnPasscodeCreateSuccess)
            }
        } else {
            updateState { it.copy(currentPasscodeInput = "", filledDots = 0) }
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
        resetPasscodeEntryStates()
    }

    private fun handleEnterPasscode() {
        if (finalConfirmationPasscodeBuilder.toString() == _state.value.loadedPasscode) {
            emitEvent(PasscodeEvent.OnUnlockSuccess)
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
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

    private fun dataArmageddon() {
        adapter.deletePasscode()
        updateState { it.copy(loadedPasscode = null, passcodeStep = PasscodeStep.Create) }
        biometricsDataArmageddon()
        resetPasscodeEntryStates()
    }

    private fun biometricsDataArmageddon() {
        adapter.deleteRegistrationData()
        updateState {
            it.copy(isBiometricEnabled = false)
        }
        resetPasscodeEntryStates()
    }

    private fun updateState(update: (PasscodeState) -> PasscodeState) {
        _state.update { update(it) }
    }

    private fun emitEvent(event: PasscodeEvent) = scope.launch {
        _events.send(event)
    }

    /**
     * Sends a [PasscodeAction] to the manager's action channel.
     *
     * @param action The action to send.
     */
    fun trySendAction(action: PasscodeAction) {
        actions.trySend(action)
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
 * @property isBiometricEnabled Whether biometric authentication is enabled and registered.
 */
data class PasscodeState(
    val filledDots: Int = 0,
    val passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val loadedPasscode: String? = null,
    val passcodeStep: PasscodeStep = PasscodeStep.Unset,
    val isChangeFlow: Boolean = false,
    val isBiometricEnabled: Boolean = false,
)

/**
 * Events emitted by the [PasscodeManager] to notify the UI or other components of state changes.
 */
sealed interface PasscodeEvent {
    /** Emitted when the passcode or biometric authentication is successful. */
    object OnUnlockSuccess : PasscodeEvent

    /** Emitted when a new passcode is successfully created and confirmed for the first time. */
    object OnPasscodeCreateSuccess : PasscodeEvent

    /** Emitted when an existing passcode is successfully changed. */
    object OnPasscodeChanged : PasscodeEvent

    /** Emitted when biometric authentication is successfully disabled. */
    object OnDisableBiometricsSuccess : PasscodeEvent

    /** Emitted when an incorrect passcode is entered during the unlock or change verification flow. */
    object OnRejectEnteredPasscode : PasscodeEvent

    /** Emitted when the confirmation passcode does not match the creation passcode. */
    object OnRejectConfirmationPasscode : PasscodeEvent

    /** Emitted when the passcode is deleted (e.g., via "Forgot Passcode"). */
    object OnPasscodeDeletion : PasscodeEvent

    /**
     * Emitted when biometric authentication fails.
     * @property message An optional error message explaining the failure.
     */
    data class OnBiometricUnlockFailure(val message: String?) : PasscodeEvent

    /** Emitted when a biometric unlock is attempted but the user is not registered. */
    object OnBiometricUserNotRegistered : PasscodeEvent
}

/**
 * Actions that can be sent to the [PasscodeManager] to trigger logic.
 */
sealed interface PasscodeAction {
    /** Deletes the passcode and biometric data, then resets to the [PasscodeStep.Create] step. */
    object ForgetPasscode : PasscodeAction

    /** Erases the passcode and biometric data silently (no event emitted). Useful for logout. */
    object LogOutErase : PasscodeAction

    /** Initiates the flow to change the existing passcode. */
    object ChangePasscode : PasscodeAction

    /** Toggles the visibility of the entered passcode characters. */
    object TogglePasscodeVisibility : PasscodeAction

    /** Deletes the last entered digit. */
    object DeleteKey : PasscodeAction

    /** Deletes all currently entered digits in the current step. */
    object DeleteAllKeys : PasscodeAction

    /**
     * Enters a single digit.
     * @property key The digit to enter.
     */
    data class EnterKey(val key: String) : PasscodeAction

    /**
     * Updates the required passcode length.
     * @property length The new length ([PasscodeLength.FOUR_DIGIT] or [PasscodeLength.SIX_DIGIT]).
     */
    data class UpdatePasscodeLength(val length: PasscodeLength) : PasscodeAction

    /** Signals a successful biometric authentication. */
    object BiometricUnlockSuccess : PasscodeAction

    /**
     * Signals a failed biometric authentication.
     * @property message An optional error message.
     */
    data class BiometricUnlockFailure(val message: String? = null) : PasscodeAction

    /** Signals that the user is not registered for biometrics. */
    object BiometricUserNotRegistered : PasscodeAction

    /** Initiates the flow to disable biometric authentication (requires passcode verification). */
    object DisableBiometrics : PasscodeAction

    /**
     * Saves biometric registration data.
     * @property registrationData The opaque data string to save.
     */
    data class SaveBiometricRegistration(val registrationData: String) : PasscodeAction

    /** Deletes stored biometric registration data. */
    object DeleteBiometricRegistration : PasscodeAction
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

    /** User is verifying their passcode to disable biometrics. */
    DisableBiometrics,
}
