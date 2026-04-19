/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.platformAuthentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.screen.PasscodeScreen

/**
 * Wraps [PasscodeScreen] with biometric-button integration.
 *
 * Bridges the biometrics and passcode libraries, which never import each other.
 * Surfaces two independent callbacks:
 *  - [onPasscodeResult] for passcode-flow events (Verified, Created, Changed, Forgotten, Rejected)
 *  - [onBiometricSuccess] for a biometric authentication succeeding
 *
 * Biometric success is **not** translated into a [PasscodeResult] — it is delivered via
 * its own callback so neither library leaks the other's concepts. The consumer decides
 * whether the two paths converge to the same destination.
 *
 * Must be hosted inside a `PlatformAuthenticatorLocalCompositionProvider` so the
 * `platformAuthenticationProvider` CompositionLocal is available.
 */
@Composable
fun PasscodeScreenWithBiometrics(
    passcodeManager: PasscodeManager,
    onPasscodeResult: (PasscodeResult) -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricError: (String) -> Unit = {},
    appName: String = "Unlock with Biometrics",
) {
    val authProvider = platformAuthenticationProvider.current
    val isRegistered by authProvider.isRegistered.collectAsState()

    PasscodeScreen(
        passcodeManager = passcodeManager,
        onResult = onPasscodeResult,
        isExternalAuthEnabled = isRegistered,
        externalAuthButton = { modifier ->
            BiometricKey(
                modifier = modifier,
                appName = appName,
                onSuccess = onBiometricSuccess,
                onUserNotRegistered = { /* no-op; isRegistered flips via provider */ },
                onAuthenticationError = onBiometricError,
            )
        },
    )
}
