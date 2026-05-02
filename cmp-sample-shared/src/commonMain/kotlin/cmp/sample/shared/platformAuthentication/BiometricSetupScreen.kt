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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmp.sample.shared.theme.blueTint
import kotlinx.coroutines.launch
import mifos_authenticator.cmp_sample_shared.generated.resources.Res
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_setup_confirm
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_setup_description
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_setup_headline
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_setup_skip
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_setup_title
import mifos_authenticator.cmp_sample_shared.generated.resources.biometrics_not_available_message
import mifos_authenticator.cmp_sample_shared.generated.resources.biometrics_not_set_up_message
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.passcode.components.MifosIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupScreen(
    onBiometricsRegistrationSuccess: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    onError: (String) -> Unit,
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val scope = rememberCoroutineScope()

    val biometricsNotSetUpMessage = stringResource(Res.string.biometrics_not_set_up_message)
    val biometricsNotAvailableMessage = stringResource(Res.string.biometrics_not_available_message)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.biometric_setup_title)) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(40.dp))

            Text(
                text = stringResource(Res.string.biometric_setup_headline),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.biometric_setup_description),
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        val result = platformAuthenticationProvider.registerUser(
                            "mifosUser",
                            "mifos@mifos.org",
                            "Mifos User",
                        )
                        when (result) {
                            is RegistrationResult.Success -> {
                                onBiometricsRegistrationSuccess()
                            }
                            RegistrationResult.PlatformAuthenticatorNotSet -> {
                                onError(biometricsNotSetUpMessage)
                            }
                            RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                                onError(biometricsNotAvailableMessage)
                            }
                            is RegistrationResult.Error -> {
                                onError(result.message)
                            }
                            RegistrationResult.UserCancelled -> { /* User dismissed prompt, do nothing */ }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blueTint),
            ) {
                Text(stringResource(Res.string.biometric_setup_confirm), color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onSkipBiometricSetup,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.biometric_setup_skip), color = blueTint)
            }
        }
    }
}
