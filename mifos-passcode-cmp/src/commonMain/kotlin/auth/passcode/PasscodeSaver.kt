package com.mifos.passcode.auth.passcode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mifos.passcode.utility.Constants
import com.mifos.passcode.utility.Step
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A sealed interface representing passcode events
 */
sealed interface PasscodeEvent {
    data class PasscodeConfirmed(val passcode: String) : PasscodeEvent
    data object PasscodeRejected : PasscodeEvent

    data object NoPasscodeAction: PasscodeEvent
}

/**
 * A data class representing passcode UI state
 */
data class PasscodeState(
    val activeStep: Step = Step.Create,
    val filledDots: Int = 0,
    val passcodeVisible: Boolean = false,
    val currentPasscodeInput: String = "",
    val isPasscodeAlreadySet: Boolean = false,
    val attempts: Int = 0
)

/**
 * Composable function that creates and remembers a PasscodeSaver instance
 *
 * @param currentPasscode The current saved passcode, if any
 * @param isPasscodeSet Whether a passcode is already set
 * @param savePasscode Function to save a new passcode
 * @param clearPasscode Function to clear the saved passcode
 * @return A PasscodeSaver instance
 */
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

/**
 * A class that manages passcode creation, confirmation, and validation
 */
class PasscodeSaver(
    private val currentPasscode: String,
    isPasscodeSet: Boolean,
    private val savePasscode: (String) -> Unit,
    private val clearPasscode: () -> Unit,
    private val scope: CoroutineScope
) {
    // Events
    private val _events = Channel<PasscodeEvent>()
    val events = _events.receiveAsFlow()

    // State
    private val _state = MutableStateFlow(PasscodeState(isPasscodeAlreadySet = isPasscodeSet))
    val state: StateFlow<PasscodeState> = _state.asStateFlow()

    var attempts = 0;

    // Internal data
    private var createPasscode = StringBuilder()
    private var confirmPasscode = StringBuilder()

    init {
        restart()
    }

    /**
     * Updates the state with new values
     */
    private fun updateState(update: PasscodeState.() -> PasscodeState) {
        _state.value = _state.value.update()
    }

    /**
     * Emits a passcode event
     */
    private fun emitEvent(event: PasscodeEvent) = scope.launch {
        _events.trySend(event)
    }

    /**
     * Toggles passcode visibility
     */
    fun togglePasscodeVisibility() {
        updateState { copy(passcodeVisible = !passcodeVisible) }
    }

    /**
     * Restarts the passcode entry process
     */
    fun restart() {
        createPasscode.clear()
        confirmPasscode.clear()
        updateState {
            copy(
                activeStep = Step.Create,
                filledDots = 0,
                currentPasscodeInput = ""
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
        if (currentState.filledDots >= Constants.PASSCODE_LENGTH) {
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
                filledDots = passcodeBuilder.length
            )
        }

        // Handle completed passcode entry
        if (passcodeBuilder.length == Constants.PASSCODE_LENGTH) {
            handleCompletedPasscodeEntry()
        }
    }

    /**
     * Gets the active passcode builder based on current step
     */
    private fun getActivePasscodeBuilder(): StringBuilder {
        return if (_state.value.activeStep == Step.Create) createPasscode else confirmPasscode
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
                    emitEvent(PasscodeEvent.PasscodeConfirmed(createPasscode.toString()))
                    createPasscode.clear()
                } else {
                    emitEvent(PasscodeEvent.PasscodeRejected)
                    attempts++
                    _state.update {
                        it.copy(
                            attempts = this.attempts
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
                        currentPasscodeInput = ""
                    )
                }
            }

            // Confirming a new passcode
            else -> {
                if (createPasscode.toString() == confirmPasscode.toString()) {
                    val confirmedPasscode = confirmPasscode.toString()
                    emitEvent(PasscodeEvent.PasscodeConfirmed(confirmedPasscode))
                    savePasscode(confirmedPasscode)
                    updateState { copy(isPasscodeAlreadySet = true) }
                    restart()
                } else {
                    emitEvent(PasscodeEvent.PasscodeRejected)
                    restart()
                }
            }
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
                    filledDots = passcodeBuilder.length
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
        clearPasscode()
        updateState {
            copy(
                isPasscodeAlreadySet = false,
                passcodeVisible = false
            )
        }
        restart()
    }
}