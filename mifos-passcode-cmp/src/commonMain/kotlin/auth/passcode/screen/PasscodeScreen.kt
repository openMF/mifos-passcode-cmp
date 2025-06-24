package com.mifos.passcode.auth.passcode.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.unit.dp
import com.mifos.passcode.PasscodeEvent
import com.mifos.passcode.PasscodeSaver
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.auth.passcode.components.PasscodeForgotButton
import com.mifos.passcode.auth.passcode.components.PasscodeHeader
import com.mifos.passcode.auth.passcode.components.PasscodeLengthChangeButton
import com.mifos.passcode.auth.passcode.components.PasscodeMismatchedDialog
import com.mifos.passcode.auth.passcode.components.PasscodeSkipButton
import com.mifos.passcode.auth.passcode.components.PasscodeToolbar
import com.mifos.passcode.auth.passcode.components.Visibility
import com.mifos.passcode.auth.passcode.components.VisibilityOff
import com.mifos.passcode.ui.component.PasscodeKeys
import com.mifos.passcode.ui.theme.blueTint
import com.mifos.passcode.utility.ShakeAnimation.performShakeAnimation


@Composable
fun PasscodeScreen(
    passcodeSaver: PasscodeSaver,
    onForgotButton: () -> Unit,
    onPasscodeRejected: () -> Unit = {},
    onPasscodeConfirm: (String) -> Unit,
    onInvalidPasscodeData: () -> Unit,
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

            is PasscodeEvent.InvalidPasscodeData -> {
                snackBarHostState.showSnackbar(
                    (events as PasscodeEvent.InvalidPasscodeData).message,
                )

                onInvalidPasscodeData()
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(
                    top = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PasscodeToolbar (
                activeStep = state.activeStep,
                state.isPasscodeAlreadySet
            )


            MifosIcon(modifier = Modifier.fillMaxWidth())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                PasscodeHeader(
                    activeStep = state.activeStep,
                    isPasscodeAlreadySet = state.isPasscodeAlreadySet
                )

                Spacer(modifier = Modifier.height(20.dp))

                PasscodeView(
                    filledDots = state.filledDots,
                    passcodeLength = state.passcodeLength,
                    currentPasscode = state.currentPasscodeInput,
                    passcodeVisible = state.passcodeVisible,
                    restart = { passcodeSaver.restart() },
                    passcodeRejectedDialogVisible = passcodeRejectedDialogVisible,
                    onDismissDialog = { passcodeRejectedDialogVisible = false },
                    xShake = xShake
                )

            }

//            Spacer(modifier = Modifier.height(4.dp))

            PasscodeKeys(
                enterKey = { passcodeSaver.enterKey(it) },
                deleteKey = { passcodeSaver.deleteKey() },
                deleteAllKeys = { passcodeSaver.deleteAllKeys() },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            PasscodeForgotButton(
                onForgotButton = {
                    passcodeSaver.forgetPasscode()
                    onForgotButton.invoke()
                },
                hasPassCode = state.isPasscodeAlreadySet
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasscodeLengthChangeButton(
                onPasscodeLengthChange = {
                    passcodeSaver.changePasscodeChangeSwitchState()
                },
                hasPassCode = state.isPasscodeAlreadySet,
                state.passcodeLengthSwitchChecked
            )

        }
    }
}

@Composable
private fun PasscodeView(
    modifier: Modifier = Modifier,
    restart: () -> Unit,
    passcodeLength: Int,
    filledDots: Int,
    passcodeVisible: Boolean,
    currentPasscode: String,
    passcodeRejectedDialogVisible: Boolean,
    onDismissDialog: () -> Unit,
    xShake: Animatable<Float, *>
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
                        color = blueTint
                    )
                } else {
                    val isFilledDot = dotIndex + 1 <= filledDots
                    val dotColor = animateColorAsState(
                        if (isFilledDot) blueTint else Color.Gray,
                        label = ""
                    )

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = dotColor.value,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
//        IconButton(
//            onClick = { togglePasscodeVisibility.invoke() },
//            modifier = Modifier.padding(start = 10.dp)
//        ) {
//            Icon(
//                imageVector = if (passcodeVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
//                contentDescription = null
//            )
//        }
    }
}
