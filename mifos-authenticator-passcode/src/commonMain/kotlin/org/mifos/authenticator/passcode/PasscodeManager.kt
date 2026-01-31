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
import kotlinx.coroutines.launch
import org.mifos.authenticator.passcode.utility.PasscodeLength

@Composable
fun rememberPasscodeManager(
    adapter: PasscodeStorageAdapter,
    scope: CoroutineScope,
): PasscodeManager {
    return remember(scope) {
        PasscodeManager(adapter, scope).initialize()
    }
}

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
        if (loaded != null) {
            updateState {
                copy(
                    loadedPasscode = loaded,
                    passcodeStep = PasscodeStep.Enter,
                )
            }
        } else {
            updateState {
                copy(passcodeStep = PasscodeStep.Create)
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

    fun handleAction(action: PasscodeAction) {
        when (action) {
            PasscodeAction.ChangePasscode -> changePasscode()
            PasscodeAction.DeleteAllKeys -> deleteAllKeys()
            PasscodeAction.DeleteKey -> deleteKey()
            is PasscodeAction.EnterKey -> enterKey(action.key)
            PasscodeAction.DeletePasscode -> deletePasscode()
            PasscodeAction.TogglePasscodeVisibility -> togglePasscodeVisibility()
            is PasscodeAction.UpdatePasscodeLength -> updatePasscodeLength(action.length)
        }
    }

    private fun updatePasscodeLength(length: PasscodeLength) {
        updateState {
            copy(
                passcodeLength = length,
            )
        }
    }

    private fun deleteKey() {
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

    private fun deleteAllKeys() {
        getActivePasscodeBuilder().clear()
        updateState { copy(currentPasscodeInput = "", filledDots = 0) }
    }

    private fun enterKey(key: String) {
        val currentState = _state.value

        if (currentState.filledDots >= _state.value.passcodeLength.length) {
            return
        }

        val passcodeBuilder = getActivePasscodeBuilder()

        passcodeBuilder.append(key)

        updateState {
            copy(
                currentPasscodeInput = passcodeBuilder.toString(),
                filledDots = passcodeBuilder.length,
            )
        }

        if (passcodeBuilder.length == _state.value.passcodeLength.length) {
            handleCompletedPasscodeEntry()
        }
    }

    private fun togglePasscodeVisibility() {
        updateState { copy(passcodeVisible = !passcodeVisible) }
    }

    private fun changePasscode() {
        updateState {
            copy(passcodeStep = PasscodeStep.ChangeVerify)
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
        val loadedPasscode = _state.value.loadedPasscode
        if (finalConfirmationPasscodeBuilder.toString() == loadedPasscode) {
            updateState {
                copy(passcodeStep = PasscodeStep.Create)
            }
        } else {
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
        clearStates()
    }

    private fun handleCreatePasscode() {
        updateState {
            copy(
                currentPasscodeInput = "",
                filledDots = 0,
                passcodeVisible = false,
                passcodeStep = PasscodeStep.Confirm,
            )
        }
    }

    private fun handleConfirmPasscode() {
        if (creationPasscodeBuilder.toString() == finalConfirmationPasscodeBuilder.toString()) {
            updateState {
                copy(
                    currentPasscodeInput = "",
                    filledDots = 0,
                    passcodeVisible = false,
                    passcodeStep = PasscodeStep.Enter,
                )
            }
            creationPasscodeBuilder.clear()
            adapter.savePasscode(finalConfirmationPasscodeBuilder.toString())
            emitEvent(PasscodeEvent.OnCreateSuccess)
        } else {
            updateState {
                copy(
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
            copy(
                filledDots = 0,
                currentPasscodeInput = "",
                passcodeVisible = false,
                passcodeLength = when (loadedPasscode?.length) {
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
        clearStates()
        emitEvent(PasscodeEvent.OnPasscodeDeletion)
    }

    private fun updateState(update: PasscodeState.() -> PasscodeState) {
        _state.value = _state.value.update()
    }

    private fun emitEvent(event: PasscodeEvent) = scope.launch {
        _events.send(event)
    }

    private fun sendAction(action: PasscodeAction) {
        scope.launch {
            actions.send(action)
        }
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
)

sealed interface PasscodeEvent {
    object OnUnlockSuccess : PasscodeEvent
    object OnCreateSuccess : PasscodeEvent
    object OnRejectEnteredPasscode : PasscodeEvent
    object OnRejectConfirmationPasscode : PasscodeEvent
    object OnPasscodeDeletion : PasscodeEvent
}

sealed interface PasscodeAction {
    object DeletePasscode : PasscodeAction
    object ChangePasscode : PasscodeAction
    object TogglePasscodeVisibility : PasscodeAction
    object DeleteKey : PasscodeAction
    object DeleteAllKeys : PasscodeAction
    data class EnterKey(val key: String) : PasscodeAction
    data class UpdatePasscodeLength(val length: PasscodeLength) : PasscodeAction
}

enum class PasscodeStep(val index: Int) {
    Unset(-1),
    Enter(-1),
    Create(1),
    Confirm(2),
    ChangeVerify(0),
}
