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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthOptions
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.authenticator.biometrics.platformAvailableAuthenticationOption
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.components.PasscodeKey

@Composable
fun BiometricKey(
    modifier: Modifier,
    passcodeManager: PasscodeManager,
    onUserNotRegistered: () -> Unit,
    onAuthenticationError: (String) -> Unit,
    biometricStorageAdapter: BiometricStorageAdapter = koinInject(),
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val platformAvailableAuthenticationOption = platformAvailableAuthenticationOption.current
    val platformAuthOptions by platformAvailableAuthenticationOption.currentAuthOption.collectAsState()
    val authenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsState()
    val scope = rememberCoroutineScope()

    val isBiometricAvailable = authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET) ||
        authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)

    if (isBiometricAvailable) {
        val icon: ImageVector = when {
            platformAuthOptions.contains(PlatformAuthOptions.Fingerprint) -> Icons.Default.Fingerprint
            platformAuthOptions.contains(PlatformAuthOptions.FaceId) -> Icons.Default.Face
            else -> Icons.Default.Lock
        }

        PasscodeKey(
            modifier = modifier,
            keyIcon = icon,
            onClick = {
                scope.launch {
                    val result = platformAuthenticationProvider.onAuthenticatorClick(
                        "Unlock with Biometrics",
                        biometricStorageAdapter.loadRegistrationData() ?: "",
                    )
                    when (result) {
                        is AuthenticationResult.Success -> {
                            passcodeManager.notifyExternalAuthSuccess()
                        }
                        is AuthenticationResult.Error -> {
                            onAuthenticationError(result.message)
                        }
                        is AuthenticationResult.UserNotRegistered -> {
                            passcodeManager.setExternalAuthEnabled(false)
                            biometricStorageAdapter.deleteRegistrationData()
                            onUserNotRegistered()
                        }
                        AuthenticationResult.UserCancelled -> { }
                    }
                }
            },
        )
    }
}
