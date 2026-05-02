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
import mifos_authenticator.cmp_sample_shared.generated.resources.Res
import mifos_authenticator.cmp_sample_shared.generated.resources.biometrics_not_available_message
import mifos_authenticator.cmp_sample_shared.generated.resources.biometrics_not_set_up_message
import mifos_authenticator.cmp_sample_shared.generated.resources.change_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.disable_biometrics
import mifos_authenticator.cmp_sample_shared.generated.resources.enable_biometrics
import mifos_authenticator.cmp_sample_shared.generated.resources.home_screen_title
import mifos_authenticator.cmp_sample_shared.generated.resources.log_out
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.passcode.PasscodeManager

@Composable
fun HomeScreen(
    usingPasscode: Boolean,
    onLogoutClick: () -> Unit,
    navigateToPasscodeScreen: () -> Unit,
    navigateToDisableBiometricVerify: () -> Unit,
    onBiometricsEnableError: (String) -> Unit,
    passcodeManager: PasscodeManager = koinInject<PasscodeManager>(),
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val isRegistered by platformAuthenticationProvider.isRegistered.collectAsState()
    val scope = rememberCoroutineScope()

    val biometricsNotSetUpMessage = stringResource(Res.string.biometrics_not_set_up_message)
    val biometricsNotAvailableMessage = stringResource(Res.string.biometrics_not_available_message)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(Res.string.home_screen_title),
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
            Text(stringResource(Res.string.log_out))
        }

        if (usingPasscode) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    passcodeManager.changePasscode()
                    navigateToPasscodeScreen()
                },
            ) {
                Text(stringResource(Res.string.change_passcode))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isRegistered) {
                        navigateToDisableBiometricVerify()
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
                                    onBiometricsEnableError(biometricsNotSetUpMessage)
                                }
                                RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                                    onBiometricsEnableError(biometricsNotAvailableMessage)
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
                    stringResource(
                        if (isRegistered) Res.string.disable_biometrics else Res.string.enable_biometrics,
                    ),
                )
            }
        }
    }
}
