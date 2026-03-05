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

    val state = _state.asStateFlow()

    private val _events = Channel<PasscodeEvent>(capacity = Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private val _actions = Channel<PasscodeAction>(capacity = Channel.UNLIMITED)
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

    fun handleAction(action: PasscodeAction) {
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

    fun trySendAction(action: PasscodeAction) {
        actions.trySend(action)
    }
}

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

sealed interface PasscodeEvent {
    object OnUnlockSuccess : PasscodeEvent
    object OnPasscodeCreateSuccess : PasscodeEvent
    object OnPasscodeChanged : PasscodeEvent
    object OnDisableBiometricsSuccess : PasscodeEvent
    object OnRejectEnteredPasscode : PasscodeEvent
    object OnRejectConfirmationPasscode : PasscodeEvent
    object OnPasscodeDeletion : PasscodeEvent
    data class OnBiometricUnlockFailure(val message: String?) : PasscodeEvent
    object OnBiometricUserNotRegistered : PasscodeEvent
}

sealed interface PasscodeAction {
    object ForgetPasscode : PasscodeAction
    object LogOutErase : PasscodeAction
    object ChangePasscode : PasscodeAction
    object TogglePasscodeVisibility : PasscodeAction
    object DeleteKey : PasscodeAction
    object DeleteAllKeys : PasscodeAction
    data class EnterKey(val key: String) : PasscodeAction
    data class UpdatePasscodeLength(val length: PasscodeLength) : PasscodeAction
    object BiometricUnlockSuccess : PasscodeAction
    data class BiometricUnlockFailure(val message: String? = null) : PasscodeAction
    object BiometricUserNotRegistered : PasscodeAction
    object DisableBiometrics : PasscodeAction
    data class SaveBiometricRegistration(val registrationData: String) : PasscodeAction
    object DeleteBiometricRegistration : PasscodeAction
}

enum class PasscodeStep {
    Unset,
    Enter,
    Create,
    Confirm,
    ChangeVerify,
    DisableBiometrics,
}
