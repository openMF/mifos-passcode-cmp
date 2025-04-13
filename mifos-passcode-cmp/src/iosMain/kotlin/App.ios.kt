package com.mifos.passcode

import androidx.compose.ui.window.ComposeUIViewController
import com.mifos.passcode.biometric.domain.BioMetricUtil
import com.mifos.passcode.ui.screen.PasscodeScreen
import com.mifos.passcode.ui.viewmodels.BiometricAuthorizationViewModel
import platform.UIKit.UIViewController

fun MainViewController(
    bioMetricUtil: BioMetricUtil,
    biometricViewModel: BiometricAuthorizationViewModel): UIViewController = ComposeUIViewController {
    PasscodeScreen(
        onPasscodeConfirm = {},
        onSkipButton = {
        },
        onForgotButton = {},
        onPasscodeRejected = {},
        bioMetricUtil = bioMetricUtil,
        platformAuthenticatorViewModel = biometricViewModel,
        onBiometricAuthSuccess = {
         },
        enableSystemAuthentication = true
    )
}