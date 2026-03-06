/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.passcode.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_logo
import org.jetbrains.compose.resources.painterResource
import org.mifos.authenticator.passcode.PasscodeAction
import org.mifos.authenticator.passcode.PasscodeEvent
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStep
import org.mifos.authenticator.passcode.components.MifosIcon
import org.mifos.authenticator.passcode.components.PasscodeForgotButton
import org.mifos.authenticator.passcode.components.PasscodeHeader
import org.mifos.authenticator.passcode.components.PasscodeKeys
import org.mifos.authenticator.passcode.components.PasscodeLengthSwitch
import org.mifos.authenticator.passcode.components.PasscodeMismatchedDialog
import org.mifos.authenticator.passcode.theme.changePasscodeLengthStyle
import org.mifos.authenticator.passcode.theme.forgotButtonStyle
import org.mifos.authenticator.passcode.theme.passcodeKeyButtonStyle
import org.mifos.authenticator.passcode.utility.PasscodeLength
import org.mifos.authenticator.passcode.utility.ShakeAnimation.performShakeAnimation

/**
 * A composable function that displays a comprehensive passcode entry screen.
 *
 * This screen handles various passcode flows, including creation, entry, and changing a passcode,
 * and integrates with the [PasscodeManager] to manage its state and logic. It offers extensive
 * customization options through several configuration objects.
 *
 * @param passcodeManager The [PasscodeManager] instance responsible for handling passcode logic.
 * @param onForgotButton Lambda to be invoked when the "Forgot Passcode" button is pressed.
 * @param onPasscodeConfirm Lambda to be invoked when an existing passcode is successfully entered.
 * @param onPasscodeCreation Lambda to be invoked when a new passcode is successfully created.
 * @param onPasscodeChanged Lambda to be invoked when the passcode is successfully changed.
 * @param onPasscodeRejected Lambda to be invoked when an entered passcode (for unlock or change verification) is incorrect.
 * @param onDisableBiometrics Lambda to be invoked when biometrics are successfully disabled.
 * @param onBiometricError Lambda to be invoked when a biometric authentication error occurs.
 * @param modifier Optional [Modifier] for the screen's root layout.
 * @param appearanceConfig Configuration for the overall visual appearance of the screen.
 * @param logoConfig Configuration for the logo displayed on the screen.
 * @param dotConfig Configuration for the passcode input dots.
 * @param keyConfig Configuration for the passcode input keys (numbers and actions).
 * @param buttonConfig Configuration for action buttons like "Skip" and "Forgot".
 * @param switchConfig Configuration for the passcode length switch.
 * @param dialogConfig Configuration for the "Passcode Mismatched" dialog.
 * @param biometricButton Optional composable to display a biometric authentication button.
 */
@Composable
fun PasscodeScreen(
    passcodeManager: PasscodeManager,
    onForgotButton: () -> Unit,
    onPasscodeConfirm: () -> Unit,
    onPasscodeCreation: () -> Unit,
    onPasscodeChanged: () -> Unit = {},
    onPasscodeRejected: () -> Unit = {},
    onDisableBiometrics: () -> Unit = {},
    onBiometricError: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
    appearanceConfig: PasscodeAppearanceConfig = PasscodeAppearanceConfig(),
    logoConfig: PasscodeLogoConfig = PasscodeLogoConfig(),
    dotConfig: PasscodeDotConfig = PasscodeDotConfig(),
    keyConfig: PasscodeKeyConfig = PasscodeKeyConfig(),
    buttonConfig: PasscodeButtonConfig = PasscodeButtonConfig(),
    switchConfig: PasscodeSwitchConfig = PasscodeSwitchConfig(),
    dialogConfig: PasscodeDialogConfig = PasscodeDialogConfig(),
    biometricButton: @Composable ((Modifier) -> Unit)? = null,
) {
    val effectiveLogoConfig = logoConfig.copy(
        logoPainter = logoConfig.logoPainter ?: painterResource(resource = Res.drawable.mifos_logo),
    )
    val effectiveDialogConfig = dialogConfig.copy(
        dialogShape = dialogConfig.dialogShape ?: MaterialTheme.shapes.large,
    )
    val effectiveKeyConfig = keyConfig.copy(
        keyTextStyle = keyConfig.keyTextStyle ?: passcodeKeyButtonStyle(),
        keyElevation = keyConfig.keyElevation ?: CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    )
    val effectiveButtonConfig = buttonConfig.copy(
//        skipButtonTextStyle = buttonConfig.skipButtonTextStyle ?: skipButtonStyle(),
        forgotButtonTextStyle = buttonConfig.forgotButtonTextStyle ?: forgotButtonStyle(),
    )
    val effectiveSwitchConfig = switchConfig.copy(
        switchTextStyle = switchConfig.switchTextStyle ?: changePasscodeLengthStyle(),
    )

    val state by passcodeManager.state.collectAsStateWithLifecycle()

    val xShake = remember { Animatable(initialValue = 0.0F) }
    var passcodeRejectedDialogVisible by remember { mutableStateOf(false) }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(Unit) {
        passcodeManager.events.collect {
            when (it) {
                PasscodeEvent.OnUnlockSuccess -> {
                    onPasscodeConfirm()
                }
                PasscodeEvent.OnPasscodeCreateSuccess -> {
                    onPasscodeCreation()
                }
                PasscodeEvent.OnPasscodeChanged -> {
                    onPasscodeChanged()
                }
                PasscodeEvent.OnDisableBiometricsSuccess -> {
                    onDisableBiometrics()
                }
                PasscodeEvent.OnRejectEnteredPasscode -> {
                    passcodeRejectedDialogVisible = true
                    performShakeAnimation(xShake)
                    onPasscodeRejected()
                }
                PasscodeEvent.OnRejectConfirmationPasscode -> {
                    passcodeRejectedDialogVisible = true
                    performShakeAnimation(xShake)
                }
                PasscodeEvent.OnPasscodeDeletion -> {
                    onForgotButton()
                }
                is PasscodeEvent.OnBiometricUnlockFailure -> {
                    performShakeAnimation(xShake)
                    onBiometricError(it.message)
                }
                PasscodeEvent.OnBiometricUserNotRegistered -> {
                    onBiometricError("Biometrics not enabled or invalid biometrics registered.")
                    performShakeAnimation(xShake)
                }
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
                .background(appearanceConfig.backgroundColor)
                .padding(paddingValues)
                .padding(
                    top = 20.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(effectiveLogoConfig.logoSize),
                contentAlignment = Alignment.Center,
            ) {
                MifosIcon(
                    modifier = Modifier.fillMaxWidth(),
                    logoSize = effectiveLogoConfig.logoSize,
                    logoPainter = effectiveLogoConfig.logoPainter!!,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PasscodeHeader(
                    passcodeStep = state.passcodeStep,
                    textStyle = appearanceConfig.headerTextStyle,
                )

                Spacer(Modifier.height(10.dp))

                PasscodeView(
                    filledDots = state.filledDots,
                    passcodeLength = state.passcodeLength.length,
                    currentPasscode = state.currentPasscodeInput,
                    passcodeVisible = state.passcodeVisible,
                    passcodeRejectedDialogVisible = passcodeRejectedDialogVisible,
                    onDismissDialog = { passcodeRejectedDialogVisible = false },
                    xShake = xShake,
                    dotConfig = dotConfig,
                    dialogConfig = effectiveDialogConfig,
                )

                Spacer(Modifier.height(15.dp))

                AnimatedVisibility(state.passcodeStep == PasscodeStep.Create) {
                    PasscodeLengthSwitch(
                        modifier = Modifier.height(30.dp),
                        tabColor = effectiveSwitchConfig.switchTabColor,
                        enabledSwitchColor = effectiveSwitchConfig.switchEnabledColor,
                        enabledTextColor = effectiveSwitchConfig.switchEnabledTextColor,
                        disabledTextColor = effectiveSwitchConfig.switchDisabledTextColor,
                        textStyle = effectiveSwitchConfig.switchTextStyle!!,
                        passcodeLength = state.passcodeLength,
                        onSelectFourDigit = {
                            passcodeManager.trySendAction(PasscodeAction.UpdatePasscodeLength(PasscodeLength.FOUR_DIGIT))
                        },
                        onSelectSixDigit = {
                            passcodeManager.trySendAction(PasscodeAction.UpdatePasscodeLength(PasscodeLength.SIX_DIGIT))
                        },
                    )
                }
            }

            PasscodeKeys(
                modifier = Modifier.padding(horizontal = 12.dp),
                enterKey = {
                    passcodeManager.trySendAction(PasscodeAction.EnterKey(it))
                },
                deleteKey = {
                    passcodeManager.trySendAction(PasscodeAction.DeleteKey)
                },
                deleteAllKeys = {
                    passcodeManager.trySendAction(PasscodeAction.DeleteAllKeys)
                },
                passcodeVisible = state.passcodeVisible,
                togglePasscodeVisibility = {
                    passcodeManager.trySendAction(PasscodeAction.TogglePasscodeVisibility)
                },
                shouldJumbleKeys =
                if (state.passcodeStep == PasscodeStep.Enter) {
                    effectiveKeyConfig.shouldShuffleKeys
                } else {
                    false
                },
                keyTextStyle = effectiveKeyConfig.keyTextStyle!!,
                keyColor = effectiveKeyConfig.keyColor,
                keyShape = effectiveKeyConfig.keyShape,
                keyElevation = effectiveKeyConfig.keyElevation!!,
                keyContainerColor = effectiveKeyConfig.keyContainerColor,
                keySize = effectiveKeyConfig.keySize,
                biometricButton = if (state.passcodeStep == PasscodeStep.Enter && state.isBiometricEnabled) biometricButton else null,
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(state.passcodeStep == PasscodeStep.Enter) {
                PasscodeForgotButton(
                    onForgotButton = {
                        passcodeManager.trySendAction(PasscodeAction.ForgetPasscode)
                    },
                    textStyle = effectiveButtonConfig.forgotButtonTextStyle!!,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * A private composable that displays the visual representation of the passcode input.
 * This includes the dots (or visible characters), and handles the "Passcode Mismatched" dialog.
 *
 * @param passcodeLength The total length of the passcode.
 * @param filledDots The number of currently filled dots.
 * @param passcodeVisible A boolean indicating if the passcode characters should be visible or masked as dots.
 * @param currentPasscode The current passcode string entered by the user.
 * @param passcodeRejectedDialogVisible a boolean indicating if the "Passcode Mismatched" dialog should be visible.
 * @param onDismissDialog Lambda to be invoked when the "Passcode Mismatched" dialog is dismissed.
 * @param xShake An [Animatable] for the horizontal shake animation when an incorrect passcode is entered.
 * @param dotConfig Configuration for the visual appearance of the passcode dots.
 * @param dialogConfig Configuration for the "Passcode Mismatched" dialog.
 * @param modifier Optional [Modifier] for the root layout of this composable.
 */
@Composable
private fun PasscodeView(
    passcodeLength: Int,
    filledDots: Int,
    passcodeVisible: Boolean,
    currentPasscode: String,
    passcodeRejectedDialogVisible: Boolean,
    onDismissDialog: () -> Unit,
    xShake: Animatable<Float, *>,
    dotConfig: PasscodeDotConfig,
    dialogConfig: PasscodeDialogConfig,
    modifier: Modifier = Modifier,
) {
    PasscodeMismatchedDialog(
        visible = passcodeRejectedDialogVisible,
        onDismiss = {
            onDismissDialog.invoke()
        },
        containerColor = dialogConfig.dialogContainerColor,
        titleColor = dialogConfig.dialogTitleColor,
        buttonTextColor = dialogConfig.dialogButtonTextColor,
        shape = dialogConfig.dialogShape!!,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.offset(x = xShake.value.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = dotConfig.dotSpacing,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(passcodeLength) { dotIndex ->
                if (passcodeVisible && dotIndex < currentPasscode.length) {
                    Text(
                        text = currentPasscode[dotIndex].toString(),
                        style = dotConfig.visiblePasscodeTextStyle,
                    )
                } else {
                    val isFilledDot = dotIndex + 1 <= filledDots
                    val animatedDotColor = animateColorAsState(
                        if (isFilledDot) dotConfig.dotColor else dotConfig.inactiveDotColor,
                        label = "dotColor",
                    )

                    Box(
                        modifier = Modifier
                            .size(dotConfig.dotSize)
                            .background(
                                color = animatedDotColor.value,
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}
