package org.mifos.authenticator.passcode.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.mifos.authenticator.passcode.components.ExitWarningDialogConfig
import org.mifos.authenticator.passcode.components.PasscodeForgotButtonConfig
import org.mifos.authenticator.passcode.components.PasscodeKeyConfig
import org.mifos.authenticator.passcode.components.PasscodeLengthSwitchConfig
import org.mifos.authenticator.passcode.components.PasscodeSkipButtonConfig
import org.mifos.authenticator.passcode.components.PasscodeStepIndicatorConfig
import org.mifos.authenticator.passcode.components.exitWarningDialogConfig
import org.mifos.authenticator.passcode.components.passcodeForgotButtonConfig
import org.mifos.authenticator.passcode.components.passcodeKeyConfig
import org.mifos.authenticator.passcode.components.passcodeLengthSwitchConfig
import org.mifos.authenticator.passcode.components.passcodeSkipButtonConfig
import org.mifos.authenticator.passcode.components.passcodeStepIndicatorConfig

data class PasscodeScreenConfig(
    val passcodeScreenBackground: Color,
    val passcodeSkipButtonConfig: PasscodeSkipButtonConfig,
    val passcodeForgotButtonConfig: PasscodeForgotButtonConfig,
    val passcodeKeyConfig: PasscodeKeyConfig,
    val exitWarningDialogConfig: ExitWarningDialogConfig,
    val passcodeStepIndicatorConfig: PasscodeStepIndicatorConfig,
    val passcodeLengthSwitchConfig: PasscodeLengthSwitchConfig,
    val passcodeViewConfig: PasscodeViewConfig,
)

@Composable
fun passcodeScreenConfig(
    passcodeScreenBackground: Color = MaterialTheme.colorScheme.background,
    passcodeSkipButtonConfig: PasscodeSkipButtonConfig = passcodeSkipButtonConfig(),
    passcodeForgotButtonConfig: PasscodeForgotButtonConfig = passcodeForgotButtonConfig(),
    passcodeKeyConfig: PasscodeKeyConfig = passcodeKeyConfig(),
    exitWarningDialogConfig: ExitWarningDialogConfig = exitWarningDialogConfig(),
    passcodeStepIndicatorConfig: PasscodeStepIndicatorConfig = passcodeStepIndicatorConfig(),
    passcodeLengthSwitchConfig: PasscodeLengthSwitchConfig = passcodeLengthSwitchConfig(),
    passcodeViewConfig: PasscodeViewConfig = passcodeViewConfig(),
) = PasscodeScreenConfig(
    passcodeScreenBackground,
    passcodeSkipButtonConfig,
    passcodeForgotButtonConfig,
    passcodeKeyConfig,
    exitWarningDialogConfig,
    passcodeStepIndicatorConfig,
    passcodeLengthSwitchConfig,
    passcodeViewConfig
)