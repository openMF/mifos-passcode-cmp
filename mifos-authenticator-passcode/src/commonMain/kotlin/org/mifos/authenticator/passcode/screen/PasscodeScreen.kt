package org.mifos.authenticator.passcode.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mifos.authenticator.passcode.PasscodeEvent
import org.mifos.authenticator.passcode.PasscodeSaver
import org.mifos.authenticator.passcode.components.MifosIcon
import org.mifos.authenticator.passcode.components.PasscodeForgotButton
import org.mifos.authenticator.passcode.components.PasscodeHeader
import org.mifos.authenticator.passcode.components.PasscodeKeys
import org.mifos.authenticator.passcode.components.PasscodeLengthSwitch
import org.mifos.authenticator.passcode.components.PasscodeMismatchedDialog
import org.mifos.authenticator.passcode.components.PasscodeSkipButton
import org.mifos.authenticator.passcode.components.PasscodeToolbar
import org.mifos.authenticator.passcode.utility.PasscodeLength
import org.mifos.authenticator.passcode.utility.ShakeAnimation.performShakeAnimation
import org.mifos.authenticator.passcode.utility.Step


@Preview
@Composable
fun PasscodeScreen(
    passcodeSaver: PasscodeSaver,
    onForgotButton: () -> Unit,
    onSkipButton: () -> Unit,
    modifier: Modifier = Modifier,
    config: PasscodeScreenConfig = passcodeScreenConfig(),
    onPasscodeRejected: () -> Unit = {},
    onPasscodeConfirm: (String) -> Unit,
) {
    val state by passcodeSaver.state.collectAsState()

    val events by passcodeSaver.events.collectAsState(
        initial = PasscodeEvent.NoPasscodeAction
    )

    val xShake = remember { Animatable(initialValue = 0.0F) }
    var passcodeRejectedDialogVisible by remember { mutableStateOf(false) }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(
        key1 = events,
        key2 = state.attempts
    ) {
        when (events) {
            is PasscodeEvent.NoPasscodeAction -> {}
            is PasscodeEvent.PasscodeConfirmed -> {
                onPasscodeConfirm(
                    (events as PasscodeEvent.PasscodeConfirmed).passcode
                )
            }
            is PasscodeEvent.PasscodeRejected -> {
                passcodeRejectedDialogVisible = true
                performShakeAnimation(xShake)

                onPasscodeRejected()
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(config.passcodeScreenBackground)
                .padding(
                    top = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            PasscodeToolbar(
                activeStep = state.activeStep,
                state.isPasscodeAlreadySet,
                config.exitWarningDialogConfig,
                config.passcodeStepIndicatorConfig
            )

            PasscodeSkipButton(
                onSkipButton = onSkipButton,
                hasPassCode = state.isPasscodeAlreadySet,
                config = config.passcodeSkipButtonConfig
            )
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ){
                MifosIcon(modifier = Modifier.fillMaxWidth())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                PasscodeHeader(
                    activeStep = state.activeStep,
                )

                Spacer(Modifier.height(10.dp))

                PasscodeView(
                    filledDots = state.filledDots,
                    passcodeLength = state.passcodeLength.length,
                    currentPasscode = state.currentPasscodeInput,
                    passcodeVisible = state.passcodeVisible,
                    restart = { passcodeSaver.restart() },
                    passcodeRejectedDialogVisible = passcodeRejectedDialogVisible,
                    onDismissDialog = { passcodeRejectedDialogVisible = false },
                    xShake = xShake,
                    config = config.passcodeViewConfig
                )

                Spacer(Modifier.height(15.dp))

                if(state.activeStep == Step.Create){

                    PasscodeLengthSwitch(
                        modifier = Modifier.height(30.dp),
                        passcodeLength = state.passcodeLength,
                        onSelectFourDigit = {
                            passcodeSaver.updatePasscodeLength(PasscodeLength.FOUR_DIGIT)
                        },
                        onSelectSixDigit = {
                            passcodeSaver.updatePasscodeLength(PasscodeLength.SIX_DIGIT)
                        },
                        config = config.passcodeLengthSwitchConfig
                    )

                }
            }

            PasscodeKeys(
                modifier = Modifier.padding(horizontal = 12.dp),
                enterKey = { passcodeSaver.enterKey(it) },
                deleteKey = { passcodeSaver.deleteKey() },
                deleteAllKeys = { passcodeSaver.deleteAllKeys() },
                passcodeVisible = state.passcodeVisible,
                togglePasscodeVisibility = {
                    passcodeSaver.togglePasscodeVisibility()
                },
                config = config.passcodeKeyConfig
            )
            Spacer(modifier = Modifier.height(8.dp))

            PasscodeForgotButton(
                onForgotButton = {
                    passcodeSaver.forgetPasscode()
                    onForgotButton.invoke()
                },
                hasPassCode = state.isPasscodeAlreadySet,
                config = config.passcodeForgotButtonConfig
            )

            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}


data class PasscodeViewConfig(
    val filledDotsColor: Color,
    val unfilledDotsColor: Color,
    val viewShape: Shape,
)

@Composable
fun passcodeViewConfig(
    filledDotsColor: Color = MaterialTheme.colorScheme.primary,
    unfilledDotsColor: Color= MaterialTheme.colorScheme.surfaceContainerHighest,
    viewShape: Shape = CircleShape,
) = PasscodeViewConfig(filledDotsColor,unfilledDotsColor, viewShape)

@Composable
private fun PasscodeView(
    restart: () -> Unit,
    passcodeLength: Int,
    filledDots: Int,
    passcodeVisible: Boolean,
    currentPasscode: String,
    passcodeRejectedDialogVisible: Boolean,
    onDismissDialog: () -> Unit,
    xShake: Animatable<Float, *>,
    config: PasscodeViewConfig,
    modifier: Modifier = Modifier,
    ) {
    PasscodeMismatchedDialog(
        visible = passcodeRejectedDialogVisible,
        onDismiss = {
            onDismissDialog.invoke()
            restart()
        }
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = modifier.offset(x = xShake.value.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 26.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(passcodeLength) { dotIndex ->
                if (passcodeVisible && dotIndex < currentPasscode.length) {
                    Text(
                        text = currentPasscode[dotIndex].toString(),
                        color = config.filledDotsColor
                    )
                } else {
                    val isFilledDot = dotIndex + 1 <= filledDots
                    val dotColor = animateColorAsState(
                        if (isFilledDot) config.filledDotsColor
                        else config.unfilledDotsColor,
                        label = ""
                    )

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = dotColor.value,
                                shape = config.viewShape
                            )
                    )
                }
            }
        }

    }
}
