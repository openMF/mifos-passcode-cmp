/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.passcode.PasscodeManager

@Composable
fun HomeScreen(
    usingPasscode: Boolean,
    onLogoutClick: () -> Unit,
    navigateToPasscodeScreen: () -> Unit,
    onBiometricsEnableError: (String) -> Unit,
    passcodeManager: PasscodeManager = koinInject<PasscodeManager>(),
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val isRegistered by platformAuthenticationProvider.isRegistered.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Home Screen",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {
                scope.launch {
                    platformAuthenticationProvider.unregister()
                    passcodeManager.logOut()
                    onLogoutClick()
                }
            },
        ) {
            Text("Log Out")
        }

        if (usingPasscode) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    passcodeManager.changePasscode()
                    navigateToPasscodeScreen()
                },
            ) {
                Text("Change Passcode")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isRegistered) {
                        scope.launch { platformAuthenticationProvider.unregister() }
                    } else {
                        scope.launch {
                            val result = platformAuthenticationProvider.registerUser(
                                "mifosUser",
                                "mifos@mifos.org",
                                "Mifos User",
                            )
                            when (result) {
                                is RegistrationResult.Success -> { }
                                RegistrationResult.PlatformAuthenticatorNotSet -> {
                                    onBiometricsEnableError(
                                        "Biometrics are not set up on this device. " +
                                            "Please enable fingerprint or face unlock in your device settings, then try again.",
                                    )
                                }
                                RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                                    onBiometricsEnableError("Biometrics not available on this device")
                                }
                                is RegistrationResult.Error -> {
                                    onBiometricsEnableError(result.message)
                                }
                                RegistrationResult.UserCancelled -> { }
                            }
                        }
                    }
                },
            ) {
                Text(
                    if (isRegistered) "Disable Biometrics" else "Enable Biometrics",
                )
            }
        }
    }
}
