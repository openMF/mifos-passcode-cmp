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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_logo
import org.jetbrains.compose.resources.painterResource
import org.mifos.authenticator.core.designsystem.theme.blueTint
import org.mifos.authenticator.core.designsystem.theme.changePasscodeLengthStyle
import org.mifos.authenticator.core.designsystem.theme.forgotButtonStyle
import org.mifos.authenticator.core.designsystem.theme.passcodeKeyButtonStyle
import org.mifos.authenticator.core.designsystem.theme.skipButtonStyle
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


@Composable
fun PasscodeScreen(
    passcodeSaver: PasscodeSaver,
    onForgotButton: () -> Unit,
    onSkipButton: () -> Unit,
    onPasscodeRejected: () -> Unit = {},
    onPasscodeConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    logoSize: Dp = 180.dp,
    logoPainter: Painter = painterResource(resource = Res.drawable.mifos_logo),
    headerTextStyle: TextStyle = TextStyle(fontSize = 20.sp),
    dotColor: Color = blueTint,
    inactiveDotColor: Color = Color.Gray,
    dotSize: Dp = 14.dp,
    dotSpacing: Dp = 26.dp,
    visiblePasscodeTextStyle: TextStyle = TextStyle(color = blueTint, fontSize = 24.sp),
    shouldShuffleKeys: Boolean = true,
    keyTextStyle: TextStyle = passcodeKeyButtonStyle(),
    keyColor: Color = blueTint,
    keyShape: Shape = CircleShape,
    keyElevation: CardElevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    keyContainerColor: Color = Color.White,
    keySize: Dp = 60.dp,
    skipButtonTextStyle: TextStyle = skipButtonStyle(),
    forgotButtonTextStyle: TextStyle = forgotButtonStyle(),
    switchTabColor: Color = blueTint,
    switchEnabledColor: Color = Color.LightGray.copy(alpha = .7f),
    switchEnabledTextColor: Color = Color.Black,
    switchDisabledTextColor: Color = Color.White,
    switchTextStyle: TextStyle = changePasscodeLengthStyle(),
    toolbarIndicatorActiveColor: Color = blueTint,
    toolbarIndicatorInactiveColor: Color = Color.Gray,
    dialogContainerColor: Color = Color.White,
    dialogTitleColor: Color = Color.Black,
    dialogButtonTextColor: Color = Color.Black,
    dialogShape: Shape = MaterialTheme.shapes.large
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
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(
                    top = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            PasscodeToolbar(
                activeStep = state.activeStep,
                hasPasscode = state.isPasscodeAlreadySet,
                indicatorActiveColor = toolbarIndicatorActiveColor,
                indicatorInactiveColor = toolbarIndicatorInactiveColor
            )

            PasscodeSkipButton(
                onSkipButton = onSkipButton,
                hasPassCode = state.isPasscodeAlreadySet,
                textStyle = skipButtonTextStyle
            )
            Box(
                modifier = Modifier.size(logoSize),
                contentAlignment = Alignment.Center
            ){
                MifosIcon(
                    modifier = Modifier.fillMaxWidth(),
                    logoSize = logoSize,
                    logoPainter = logoPainter
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                PasscodeHeader(
                    activeStep = state.activeStep,
                    isPasscodeAlreadySet = state.isPasscodeAlreadySet,
                    textStyle = headerTextStyle
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
                    dotColor = dotColor,
                    inactiveDotColor = inactiveDotColor,
                    dotSize = dotSize,
                    dotSpacing = dotSpacing,
                    passcodeTextStyle = visiblePasscodeTextStyle,
                    dialogContainerColor = dialogContainerColor,
                    dialogTitleColor = dialogTitleColor,
                    dialogButtonTextColor = dialogButtonTextColor,
                    dialogShape = dialogShape
                )

                Spacer(Modifier.height(15.dp))

                if(state.activeStep == Step.Create){

                    PasscodeLengthSwitch(
                        modifier = Modifier.height(30.dp),
                        tabColor = switchTabColor,
                        enabledSwitchColor = switchEnabledColor,
                        enabledTextColor = switchEnabledTextColor,
                        disabledTextColor = switchDisabledTextColor,
                        textStyle = switchTextStyle,
                        passcodeLength = state.passcodeLength,
                        onSelectFourDigit = {
                            passcodeSaver.updatePasscodeLength(PasscodeLength.FOUR_DIGIT)
                        },
                        onSelectSixDigit = {
                            passcodeSaver.updatePasscodeLength(PasscodeLength.SIX_DIGIT)
                        }
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
                shouldJumbleKeys =
                    if(state.activeStep == Step.Enter) shouldShuffleKeys
                    else false,
                keyTextStyle = keyTextStyle,
                keyColor = keyColor,
                keyShape = keyShape,
                keyElevation = keyElevation,
                keyContainerColor = keyContainerColor,
                keySize = keySize
            )
            Spacer(modifier = Modifier.height(8.dp))

            PasscodeForgotButton(
                onForgotButton = {
                    passcodeSaver.forgetPasscode()
                    onForgotButton.invoke()
                },
                hasPassCode = state.isPasscodeAlreadySet,
                textStyle = forgotButtonTextStyle
            )

            Spacer(modifier = Modifier.height(20.dp))

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
    xShake: Animatable<Float, *>,
    dotColor: Color = blueTint,
    inactiveDotColor: Color = Color.Gray,
    dotSize: Dp = 14.dp,
    dotSpacing: Dp = 26.dp,
    passcodeTextStyle: TextStyle = TextStyle(color = blueTint),
    dialogContainerColor: Color = Color.White,
    dialogTitleColor: Color = Color.Black,
    dialogButtonTextColor: Color = Color.Black,
    dialogShape: Shape = MaterialTheme.shapes.large
) {
    PasscodeMismatchedDialog(
        visible = passcodeRejectedDialogVisible,
        onDismiss = {
            onDismissDialog.invoke()
            restart()
        },
        containerColor = dialogContainerColor,
        titleColor = dialogTitleColor,
        buttonTextColor = dialogButtonTextColor,
        shape = dialogShape
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.offset(x = xShake.value.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = dotSpacing,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(passcodeLength) { dotIndex ->
                if (passcodeVisible && dotIndex < currentPasscode.length) {
                    Text(
                        text = currentPasscode[dotIndex].toString(),
                        style = passcodeTextStyle
                    )
                } else {
                    val isFilledDot = dotIndex + 1 <= filledDots
                    val animatedDotColor = animateColorAsState(
                        if (isFilledDot) dotColor else inactiveDotColor,
                        label = "dotColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .background(
                                color = animatedDotColor.value,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

    }
}
