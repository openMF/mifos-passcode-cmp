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
import androidx.compose.runtime.DisposableEffect
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
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.PasscodeStep
import org.mifos.authenticator.passcode.PasscodeStrings
import org.mifos.authenticator.passcode.defaultPasscodeStrings
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
 * This screen handles the passcode flows (creation, entry, change) via [PasscodeManager]
 * and delivers outcomes via the [onResult] callback as [PasscodeResult] values.
 *
 * The screen is agnostic to any external auth mechanism; it only exposes a composable slot
 * for an opt-in button and a visibility flag. The caller supplies both.
 *
 * @param passcodeManager The [PasscodeManager] instance responsible for handling passcode logic.
 * @param onResult Callback invoked with a [PasscodeResult] when a passcode operation completes.
 *        Handle navigation and other outcomes here.
 * @param modifier Optional [Modifier] for the screen's root layout.
 * @param appearanceConfig Configuration for the overall visual appearance of the screen.
 * @param logoConfig Configuration for the logo displayed on the screen.
 * @param dotConfig Configuration for the passcode input dots.
 * @param keyConfig Configuration for the passcode input keys (numbers and actions).
 * @param buttonConfig Configuration for action buttons like "Skip" and "Forgot".
 * @param switchConfig Configuration for the passcode length switch.
 * @param dialogConfig Configuration for the "Passcode Mismatched" dialog.
 * @param isExternalAuthEnabled Whether to render [externalAuthButton] during [PasscodeStep.Enter].
 *        The caller owns this flag — the passcode library doesn't track external auth state.
 * @param externalAuthButton Optional composable to display an external authentication button
 *        (e.g. biometrics). Rendered only when [PasscodeStep.Enter] is active and
 *        [isExternalAuthEnabled] is true.
 */
@Composable
fun PasscodeScreen(
    passcodeManager: PasscodeManager,
    onResult: (PasscodeResult) -> Unit,
    modifier: Modifier = Modifier,
    appearanceConfig: PasscodeAppearanceConfig = PasscodeAppearanceConfig(),
    logoConfig: PasscodeLogoConfig = PasscodeLogoConfig(),
    dotConfig: PasscodeDotConfig = PasscodeDotConfig(),
    keyConfig: PasscodeKeyConfig = PasscodeKeyConfig(),
    buttonConfig: PasscodeButtonConfig = PasscodeButtonConfig(),
    switchConfig: PasscodeSwitchConfig = PasscodeSwitchConfig(),
    dialogConfig: PasscodeDialogConfig = PasscodeDialogConfig(),
    isExternalAuthEnabled: Boolean = false,
    externalAuthButton: @Composable ((Modifier) -> Unit)? = null,
    strings: PasscodeStrings = defaultPasscodeStrings(),
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

    val displayDigits = strings.digits

    val xShake = remember { Animatable(initialValue = 0.0F) }
    var passcodeRejectedDialogVisible by remember { mutableStateOf(false) }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    var lastShakeTrigger by remember { mutableStateOf(state.shakeAnimationTrigger) }
    LaunchedEffect(state.shakeAnimationTrigger) {
        if (state.shakeAnimationTrigger != lastShakeTrigger) {
            lastShakeTrigger = state.shakeAnimationTrigger
            passcodeRejectedDialogVisible = true
            performShakeAnimation(xShake)
        }
    }

    DisposableEffect(passcodeManager) {
        passcodeManager.setResultCallback(onResult)
        onDispose {
            passcodeManager.setResultCallback(null)
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
                    strings = strings,
                )

                Spacer(Modifier.height(10.dp))

                PasscodeView(
                    filledDots = state.filledDots,
                    passcodeLength = state.passcodeLength.length,
                    currentPasscode = state.currentPasscodeInput,
                    passcodeVisible = state.passcodeVisible,
                    displayDigits = displayDigits,
                    passcodeRejectedDialogVisible = passcodeRejectedDialogVisible,
                    onDismissDialog = { passcodeRejectedDialogVisible = false },
                    xShake = xShake,
                    dotConfig = dotConfig,
                    dialogConfig = effectiveDialogConfig,
                    strings = strings,
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
                            passcodeManager.updatePasscodeLength(PasscodeLength.FOUR_DIGIT)
                        },
                        onSelectSixDigit = {
                            passcodeManager.updatePasscodeLength(PasscodeLength.SIX_DIGIT)
                        },
                        strings = strings,
                    )
                }
            }

            PasscodeKeys(
                modifier = Modifier.padding(horizontal = 12.dp),
                enterKey = {
                    passcodeManager.enterKey(it)
                },
                deleteKey = {
                    passcodeManager.deleteKey()
                },
                deleteAllKeys = {
                    passcodeManager.deleteAllKeys()
                },
                passcodeVisible = state.passcodeVisible,
                togglePasscodeVisibility = {
                    passcodeManager.togglePasscodeVisibility()
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
                externalAuthButton = if (state.passcodeStep == PasscodeStep.Enter && isExternalAuthEnabled) externalAuthButton else null,
                strings = strings,
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(state.passcodeStep == PasscodeStep.Enter) {
                PasscodeForgotButton(
                    onForgotButton = {
                        passcodeManager.forgetPasscode()
                    },
                    textStyle = effectiveButtonConfig.forgotButtonTextStyle!!,
                    strings = strings,
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
 * @param currentPasscode The current passcode string entered by the user (always ASCII digits).
 * @param displayDigits Locale-resolved glyphs for digits 0-9, indexed by ASCII digit value.
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
    displayDigits: List<String>,
    passcodeRejectedDialogVisible: Boolean,
    onDismissDialog: () -> Unit,
    xShake: Animatable<Float, *>,
    dotConfig: PasscodeDotConfig,
    dialogConfig: PasscodeDialogConfig,
    strings: PasscodeStrings,
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
        strings = strings,
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
                    val ch = currentPasscode[dotIndex]
                    val glyph = if (ch in '0'..'9') displayDigits[ch - '0'] else ch.toString()
                    Text(
                        text = glyph,
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
