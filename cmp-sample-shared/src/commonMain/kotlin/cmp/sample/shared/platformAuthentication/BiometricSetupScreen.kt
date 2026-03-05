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
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.passcode.PasscodeAction
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.components.MifosIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupScreen(
    onBiometricsRegistrationSuccess: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    onError: (String) -> Unit,
    passcodeManager: PasscodeManager = koinInject(),
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Setup") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Secure Your App",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Use biometrics (fingerprint or face) for faster and more secure access.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        val result = platformAuthenticationProvider.registerUser(
                            "mifosUser",
                            "mifos@mifos.org",
                            "Mifos User"
                        )
                        when (result) {
                            is RegistrationResult.Success -> {
                                passcodeManager.trySendAction(PasscodeAction.SaveBiometricRegistration(result.message))
                                onBiometricsRegistrationSuccess()
                            }
                            RegistrationResult.PlatformAuthenticatorNotSet -> {
                                passcodeManager.trySendAction(PasscodeAction.BiometricUserNotRegistered)
                            }
                            RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                                onError("Biometrics not available on this device")
                            }
                            is RegistrationResult.Error -> {
                                onError(result.message)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blueTint)
            ) {
                Text("Setup Biometrics", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onSkipBiometricSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now", color = blueTint)
            }
        }
    }
}
