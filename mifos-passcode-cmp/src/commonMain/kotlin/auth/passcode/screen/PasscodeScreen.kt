package com.mifos.passcode.auth.passcode.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.mifos.passcode.auth.passcode.PasscodeSaver
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.auth.passcode.components.PasscodeForgotButton
import com.mifos.passcode.auth.passcode.components.PasscodeHeader
import com.mifos.passcode.auth.passcode.components.PasscodeMismatchedDialog
import com.mifos.passcode.auth.passcode.components.PasscodeSkipButton
import com.mifos.passcode.auth.passcode.components.PasscodeToolbar
import com.mifos.passcode.auth.passcode.components.Visibility
import com.mifos.passcode.auth.passcode.components.VisibilityOff
import com.mifos.passcode.ui.component.PasscodeKeys
import com.mifos.passcode.ui.theme.blueTint
import com.mifos.passcode.utility.Constants.PASSCODE_LENGTH
import com.mifos.passcode.utility.ShakeAnimation.performShakeAnimation


/**
 * @author pratyush
 * @since 15/3/24
 */


@Composable
fun PasscodeScreen(
    passcodeSaver: PasscodeSaver,
    onForgotButton: () -> Unit,
    onSkipButton: () -> Unit,
    onPasscodeConfirm: (String) -> Unit,
) {
    val activeStep by passcodeSaver.activeStep.collectAsState()
    val filledDots by passcodeSaver.filledDots.collectAsState()
    val passcodeVisible by passcodeSaver.passcodeVisible.collectAsState()
    val currentPasscode by passcodeSaver.currentPasscodeInput.collectAsState()
    val xShake = remember { Animatable(initialValue = 0.0F) }
    var passcodeRejectedDialogVisible by remember { mutableStateOf(false) }

    val isPasscodeAlreadySet = passcodeSaver.isPasscodeAlreadySet.collectAsState()


    LaunchedEffect(key1 = passcodeSaver.onPasscodeConfirmed) {
        passcodeSaver.onPasscodeConfirmed.collect {
            onPasscodeConfirm(it)
        }
    }

    LaunchedEffect(key1 = passcodeSaver.onPasscodeRejected) {
        passcodeSaver.onPasscodeRejected.collect {
            passcodeRejectedDialogVisible = true
//              vibrateFeedback(context)
            performShakeAnimation(xShake)
        }
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PasscodeToolbar(
                activeStep = activeStep,
                isPasscodeAlreadySet.value
            )

            PasscodeSkipButton(
                onSkipButton = { onSkipButton.invoke() },
                hasPassCode = isPasscodeAlreadySet.value
            )

            MifosIcon(modifier = Modifier.fillMaxWidth())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PasscodeHeader(
                    activeStep = activeStep,
                    isPasscodeAlreadySet = isPasscodeAlreadySet.value
                )
                PasscodeView(
                    filledDots = filledDots,
                    currentPasscode = currentPasscode,
                    passcodeVisible = passcodeVisible,
                    togglePasscodeVisibility = { passcodeSaver.togglePasscodeVisibility() },
                    restart = { passcodeSaver.restart() },
                    passcodeRejectedDialogVisible = passcodeRejectedDialogVisible,
                    onDismissDialog = { passcodeRejectedDialogVisible = false },
                    xShake = xShake
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            PasscodeKeys(
                enterKey = { passcodeSaver.enterKey(it) },
                deleteKey = { passcodeSaver.deleteKey() },
                deleteAllKeys = { passcodeSaver.deleteAllKeys() },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            PasscodeForgotButton(
                onForgotButton = {
                    onForgotButton.invoke()
                    passcodeSaver.forgetPasscode()
                },
                hasPassCode = isPasscodeAlreadySet.value
            )
        }
    }
}


@Composable
private fun PasscodeView(
    modifier: Modifier = Modifier,
    restart: () -> Unit,
    togglePasscodeVisibility: () -> Unit,
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
            repeat(PASSCODE_LENGTH) { dotIndex ->
                if (passcodeVisible && dotIndex < currentPasscode.length) {
                    Text(
                        text = currentPasscode[dotIndex].toString(),
                        color = blueTint
                    )
                } else {
                    val isFilledDot = dotIndex + 1 <= filledDots
                    val dotColor = animateColorAsState(
                        if (isFilledDot) blueTint else Color.Gray, label = ""
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
        IconButton(
            onClick = { togglePasscodeVisibility.invoke() },
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Icon(
                imageVector = if (passcodeVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = null
            )
        }
    }
}


