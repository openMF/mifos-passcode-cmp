package com.mifos.passcode.auth.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mifos.passcode.utility.Constants
import com.mifos.passcode.utility.Step
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@Composable
fun rememberPasscodeSaver(
    currentPasscode: String,
    isPasscodeSet: Boolean,
    savePasscode: (String) -> Unit,
    clearPasscode: () -> Unit,
): PasscodeSaver {

    val scope = rememberCoroutineScope()

    return remember(
        key1 = currentPasscode,
        key2 = isPasscodeSet
    ) {
        PasscodeSaver(
            currentPasscode = currentPasscode,
            isPasscodeSet = isPasscodeSet,
            savePasscode = savePasscode,
            clearPasscode = clearPasscode,
            scope = scope
        )
    }

}

class PasscodeSaver(
    val currentPasscode: String,
    val isPasscodeSet: Boolean,
    val savePasscode: (String) -> Unit,
    val clearPasscode: () -> Unit,
    val scope: CoroutineScope
){

    private val _onPasscodeConfirmed = MutableSharedFlow<String>()
    val onPasscodeConfirmed = _onPasscodeConfirmed.asSharedFlow()

    private val _onPasscodeRejected = MutableSharedFlow<Unit>()
    val onPasscodeRejected = _onPasscodeRejected.asSharedFlow()

    private val _activeStep = MutableStateFlow(Step.Create)
    val activeStep = _activeStep.asStateFlow()

    private val _filledDots = MutableStateFlow(0)
    val filledDots = _filledDots.asStateFlow()

    private var createPasscode: StringBuilder = StringBuilder()
    private var confirmPasscode: StringBuilder = StringBuilder()

    private val _passcodeVisible = MutableStateFlow(false)
    val passcodeVisible = _passcodeVisible.asStateFlow()

    private val _currentPasscodeInput = MutableStateFlow("")
    val currentPasscodeInput = _currentPasscodeInput.asStateFlow()

    private val _isPasscodeAlreadySet = MutableStateFlow(isPasscodeSet)
    val isPasscodeAlreadySet = _isPasscodeAlreadySet.asStateFlow()


    init {
        restart()
    }

    private fun emitActiveStep(activeStep: Step) = scope.launch {
        _activeStep.emit(activeStep)
    }

    private fun emitFilledDots(filledDots: Int) = scope.launch {
        _filledDots.emit(filledDots)
    }

    private fun emitOnPasscodeConfirmed(confirmPassword: String) = scope.launch {
        _onPasscodeConfirmed.emit(confirmPassword)
    }

    private fun emitOnPasscodeRejected() = scope.launch {
        _onPasscodeRejected.emit(Unit)
    }

    fun togglePasscodeVisibility() {
        _passcodeVisible.value = !_passcodeVisible.value
    }

    fun restart() {
        emitActiveStep(Step.Create)
        emitFilledDots(0)
        createPasscode.clear()
        confirmPasscode.clear()
    }

    fun enterKey(key: String) {
        if (_filledDots.value >= Constants.PASSCODE_LENGTH) {
            return
        }

        val enteredPasscode =
            if (_activeStep.value == Step.Create) createPasscode else confirmPasscode

        enteredPasscode.append(key)

        _currentPasscodeInput.value = enteredPasscode.toString()

        emitFilledDots(enteredPasscode.length)

        if (_filledDots.value == Constants.PASSCODE_LENGTH) {
            if (_isPasscodeAlreadySet.value) {
                if (currentPasscode == createPasscode.toString()) {
                    emitOnPasscodeConfirmed(createPasscode.toString())
                    createPasscode.clear()
                } else {
                    emitOnPasscodeRejected()
                    // logic for retires can be written here
                }
                _currentPasscodeInput.value = ""
            } else if (_activeStep.value == Step.Create) {
                emitActiveStep(Step.Confirm)
                emitFilledDots(0)
                _currentPasscodeInput.value = ""
            } else {
                if (createPasscode.toString() == confirmPasscode.toString()) {
                    emitOnPasscodeConfirmed(confirmPasscode.toString())
                    savePasscode(confirmPasscode.toString())
                    _isPasscodeAlreadySet.value = true
                    restart()
                } else {
                    emitOnPasscodeRejected()
                    restart()
                }
                _currentPasscodeInput.value = ""
            }
        }
    }

    fun deleteKey() {
        val currentPasscode =
            if (_activeStep.value == Step.Create) createPasscode else confirmPasscode

        if (currentPasscode.isNotEmpty()) {
            currentPasscode.deleteAt(currentPasscode.length - 1)
            _currentPasscodeInput.value = currentPasscode.toString()
            emitFilledDots(currentPasscode.length)
        }
    }


    fun deleteAllKeys() {
        if (_activeStep.value == Step.Create) {
            createPasscode.clear()
        } else {
            confirmPasscode.clear()
        }
        _currentPasscodeInput.value = ""
        emitFilledDots(0)
    }

    // Used for forgetting passcode.
    fun forgetPasscode() {
        restart()
        clearPasscode()
        _isPasscodeAlreadySet.value = false
        _passcodeVisible.value = false
    }

}