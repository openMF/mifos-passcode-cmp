package com.mifos.passcode.auth.passcode.presentation.screen


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mifos.passcode.auth.passcode.domain.PasscodeRepository
import com.mifos.passcode.utility.Step
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mifos.passcode.core.Parcelable
import com.mifos.passcode.core.BaseViewModel
import com.mifos.passcode.core.Parcelize

private const val KEY_STATE = "passcode_state"
private const val PASSCODE_LENGTH = 4

class PasscodeViewModel(
    private val passcodeRepository: PasscodeRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PasscodeState, PasscodeEvent, PasscodeAction>(
    initialState = savedStateHandle[KEY_STATE] ?: PasscodeState(),
) {
    private var createPasscode: StringBuilder = StringBuilder()
    private var confirmPasscode: StringBuilder = StringBuilder()

    init {
        observePasscodeRepository()
    }

    private fun observePasscodeRepository() {
        viewModelScope.launch {
            val isPasscodeSet = passcodeRepository.isPasscodeSet()
            mutableStateFlow.update {
                it.copy(
                    isPasscodeAlreadySet = isPasscodeSet,
                    hasPasscode = isPasscodeSet,
                    currentPasscodeInput = "",
                    filledDots = 0,
                )
            }
        }
    }

    private fun enterKey(key: String) {
        if (state.filledDots >= PASSCODE_LENGTH) return

        val currentPasscode =
            if (state.activeStep == Step.Create) createPasscode else confirmPasscode
        currentPasscode.append(key)

        mutableStateFlow.update {
            it.copy(
                currentPasscodeInput = currentPasscode.toString(),
                filledDots = currentPasscode.length,
            )
        }

        if (state.filledDots == PASSCODE_LENGTH) {
            viewModelScope.launch {
                sendAction(PasscodeAction.Internal.ProcessCompletedPasscode)
            }
        }
    }

    private fun deleteKey() {
        val currentPasscode =
            if (state.activeStep == Step.Create) createPasscode else confirmPasscode
        if (currentPasscode.isNotEmpty()) {
            currentPasscode.deleteAt(currentPasscode.length - 1)
            mutableStateFlow.update {
                it.copy(
                    currentPasscodeInput = currentPasscode.toString(),
                    filledDots = currentPasscode.length,
                )
            }
        }
    }

    private fun deleteAllKeys() {
        if (state.activeStep == Step.Create) {
            createPasscode.clear()
        } else {
            confirmPasscode.clear()
        }
        mutableStateFlow.update {
            it.copy(
                currentPasscodeInput = "",
                filledDots = 0,
            )
        }
    }

    private fun togglePasscodeVisibility() {
        mutableStateFlow.update { it.copy(passcodeVisible = !it.passcodeVisible) }
    }

    private fun restart() {
        resetState()
    }

    private fun processCompletedPasscode() {
        viewModelScope.launch {
            when {
                state.isPasscodeAlreadySet -> validateExistingPasscode()
                state.activeStep == Step.Create -> moveToConfirmStep()
                else -> validateNewPasscode()
            }
        }
    }

    private suspend fun validateExistingPasscode() {
        val savedPasscode = passcodeRepository.getPasscode()
        if (savedPasscode == createPasscode.toString()) {
            sendEvent(PasscodeEvent.PasscodeConfirmed(createPasscode.toString()))
            createPasscode.clear()
        } else {
            sendEvent(PasscodeEvent.PasscodeRejected)
        }
        mutableStateFlow.update { it.copy(currentPasscodeInput = "") }
    }

    private fun moveToConfirmStep() {
        mutableStateFlow.update {
            it.copy(
                activeStep = Step.Confirm,
                filledDots = 0,
                currentPasscodeInput = "",
            )
        }
    }

    private suspend fun validateNewPasscode() {
        if (createPasscode.toString() == confirmPasscode.toString()) {
            passcodeRepository.savePasscode(confirmPasscode.toString())
            sendEvent(PasscodeEvent.PasscodeConfirmed(confirmPasscode.toString()))
            resetState()
        } else {
            sendEvent(PasscodeEvent.PasscodeRejected)
            resetState()
        }
    }

    private fun resetState() {
        mutableStateFlow.update {
            PasscodeState(
                hasPasscode = it.hasPasscode,
                isPasscodeAlreadySet = it.isPasscodeAlreadySet,
            )
        }
        createPasscode.clear()
        confirmPasscode.clear()
    }

    fun forgetPasscode(){
        viewModelScope.launch {
            resetState()
            mutableStateFlow.update {passcodeState ->
                PasscodeState(activeStep = Step.Create)
            }
            passcodeRepository.clearPasscode()
        }
    }

    override fun handleAction(action: PasscodeAction) {
        when (action) {
            is PasscodeAction.EnterKey -> enterKey(action.key)
            is PasscodeAction.DeleteKey -> deleteKey()
            is PasscodeAction.DeleteAllKeys -> deleteAllKeys()
            is PasscodeAction.TogglePasscodeVisibility -> togglePasscodeVisibility()
            is PasscodeAction.Restart -> restart()
            is PasscodeAction.ForgetPasscode -> forgetPasscode()
            is PasscodeAction.Internal.ProcessCompletedPasscode -> processCompletedPasscode()
        }
    }

}

data class PasscodeState(
    val hasPasscode: Boolean = false,
    val activeStep: Step = Step.Create,
    val filledDots: Int = 0,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val isPasscodeAlreadySet: Boolean = false,
)

sealed class PasscodeEvent {
    data class PasscodeConfirmed(val passcode: String) : PasscodeEvent()
    data object PasscodeRejected : PasscodeEvent()
}

sealed class PasscodeAction {
    data class EnterKey(val key: String) : PasscodeAction()
    data object DeleteKey : PasscodeAction()
    data object DeleteAllKeys : PasscodeAction()
    data object TogglePasscodeVisibility : PasscodeAction()
    data object Restart : PasscodeAction()

    data object ForgetPasscode: PasscodeAction()
    sealed class Internal : PasscodeAction() {
        data object ProcessCompletedPasscode : Internal()
    }
}
