package org.mifos.authenticator.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
        PasscodeManager(adapter, scope)
    }
}


class PasscodeManager (
    private val adapter: PasscodeStorageAdapter,
    private val scope: CoroutineScope,
) {

    private val _passcodeManagerState = MutableStateFlow<PasscodeManagerState>(PasscodeManagerState.Unset())
    val passcodeManagerState = _passcodeManagerState.asStateFlow()

    private val _state = MutableStateFlow<PasscodeState>(
        PasscodeState(
            loadedPasscode = adapter.loadPasscode()
        )
    )
    val state = _state.asStateFlow()


    private val _events = Channel<PasscodeEvent>()
    val events = _events.receiveAsFlow()

    private val _actions = Channel<PasscodeAction>()
    val actions: SendChannel<PasscodeAction> = _actions

    init {
        scope.launch {
            _actions.consumeAsFlow().collect {action ->
                handleAction(action)
            }
        }
    }

    private val creationPasscodeBuilder = StringBuilder()
    private val finalConfirmationPasscodeBuilder = StringBuilder()

    fun initialize() {
        adapter.loadPasscode()?.let {
            updateState {
                copy(loadedPasscode = it)
            }

            _passcodeManagerState.value = PasscodeManagerState.Enter()
        } ?: {
            _passcodeManagerState.value = PasscodeManagerState.Create()
        }
    }

    fun handleAction(action: PasscodeAction) {
        when(action) {
            PasscodeAction.ChangePasscode -> changePasscode()
            PasscodeAction.DeleteAllKeys -> deleteAllKeys()
            PasscodeAction.DeleteKey -> deleteKey()
            is PasscodeAction.EnterKey -> enterKey(action.key)
            PasscodeAction.ForgetPasscode -> forgetPasscode()
            PasscodeAction.TogglePasscodeVisibility -> togglePasscodeVisibility()
            is PasscodeAction.UpdatePasscodeLength -> updatePasscodeLength(action.length)
        }
    }

    private fun updatePasscodeLength(length: PasscodeLength) {
        updateState {
            copy(
                passcodeLength = length.length,
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

        if (currentState.filledDots >= _state.value.passcodeLength) {
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

        if (passcodeBuilder.length == _state.value.passcodeLength) {
            handleCompletedPasscodeEntry()
        }
    }

    private fun togglePasscodeVisibility() {
        updateState { copy(passcodeVisible = !passcodeVisible) }
    }

    private fun changePasscode() {
        _passcodeManagerState.value = PasscodeManagerState.ChangeVerify()
    }

    private fun handleCompletedPasscodeEntry() {
        when(_passcodeManagerState.value) {
            PasscodeManagerState.ChangeVerify() -> handleChangeVerifyPasscode()
            PasscodeManagerState.Confirm() -> handleConfirmPasscode()
            PasscodeManagerState.Create() -> handleCreatePasscode()
            PasscodeManagerState.Enter() -> handleEnterPasscode()
            else -> {}
        }
    }

    private fun getActivePasscodeBuilder(): StringBuilder {
        return when(_passcodeManagerState.value) {
            PasscodeManagerState.ChangeVerify(),
            PasscodeManagerState.Confirm(),
            PasscodeManagerState.Enter() -> {
                finalConfirmationPasscodeBuilder
            }
            else -> { creationPasscodeBuilder }
        }
    }

    private fun handleChangeVerifyPasscode() {
        val loadedPasscode = _state.value.loadedPasscode
        if(finalConfirmationPasscodeBuilder.equals(loadedPasscode)) {
            _passcodeManagerState.value = PasscodeManagerState.Create()
        } else {
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
    }

    private fun handleCreatePasscode() {
        updateState {
            copy(
                currentPasscodeInput  = "",
                filledDots = 0,
                passcodeVisible = false,
            )
        }
        _passcodeManagerState.value = PasscodeManagerState.Confirm()
    }

    private fun handleConfirmPasscode() {
        if(creationPasscodeBuilder == finalConfirmationPasscodeBuilder) {
            emitEvent(PasscodeEvent.OnCreateSuccess)
            _passcodeManagerState.value = PasscodeManagerState.Enter()
            adapter.savePasscode(finalConfirmationPasscodeBuilder.toString())
        } else {
            emitEvent(PasscodeEvent.OnRejectConfirmationPasscode)
        }
    }

    private fun handleEnterPasscode() {
        if(finalConfirmationPasscodeBuilder.equals(_state.value.loadedPasscode)) {
            emitEvent(PasscodeEvent.OnUnlockSuccess)
        } else {
            emitEvent(PasscodeEvent.OnRejectEnteredPasscode)
        }
    }


    private fun clearState() {
        updateState {
            copy(
                filledDots = 0,
                currentPasscodeInput = "",
                passcodeVisible = false,
                passcodeLength = 0,
                loadedPasscode = null,
            )
        }
        finalConfirmationPasscodeBuilder.clear()
        creationPasscodeBuilder.clear()
    }

    private fun forgetPasscode() {
        clearState()
        adapter.deletePasscode()
        emitEvent(PasscodeEvent.OnPasscodeDeletion)
    }

    private fun updateState(update: PasscodeState.() -> PasscodeState) {
        _state.value = _state.value.update()
    }

    private fun emitEvent(event: PasscodeEvent) = scope.launch {
        _events.trySend(event)
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
    val passcodeLength: Int = 4,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val loadedPasscode: String? = null,
)

sealed interface PasscodeEvent {
    object OnUnlockSuccess : PasscodeEvent
    object OnCreateSuccess : PasscodeEvent
    object OnRejectEnteredPasscode:  PasscodeEvent
    object OnRejectConfirmationPasscode:  PasscodeEvent
    object OnPasscodeDeletion: PasscodeEvent
}

sealed interface PasscodeAction {
    object ForgetPasscode : PasscodeAction
    object ChangePasscode: PasscodeAction
    object TogglePasscodeVisibility : PasscodeAction
    object DeleteKey : PasscodeAction
    object DeleteAllKeys : PasscodeAction
    data class EnterKey(val key: String) : PasscodeAction
    data class UpdatePasscodeLength(val length: PasscodeLength) : PasscodeAction
}

sealed class PasscodeManagerState(val index: Int) {
    class Unset(index: Int = -1) : PasscodeManagerState(index)
    class Enter(index: Int = 3) : PasscodeManagerState(index)
    class Create(index: Int = 1) : PasscodeManagerState(index)
    class Confirm(index: Int = 2) : PasscodeManagerState(index)
    class ChangeVerify(index: Int = 0) : PasscodeManagerState(index)
}