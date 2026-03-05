/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmp.sample.shared.platformAuthentication.BiometricSetupScreen
import cmp.sample.shared.ui.components.DialogBoxType
import cmp.sample.shared.ui.components.MessageDiaglogBox
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthOptions
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.authenticator.biometrics.platformAvailableAuthenticationOption
import org.mifos.authenticator.passcode.PasscodeAction
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStorageAdapter
import org.mifos.authenticator.passcode.components.PasscodeKey
import org.mifos.authenticator.passcode.screen.PasscodeScreen

@Composable
fun SampleAppNavigation(
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
) {
    val navController = rememberNavController()

    val isUsingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank()

    var dialogBoxType by remember { mutableStateOf(DialogBoxType.None) }
    var dialogMessage by remember { mutableStateOf("") }

    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val scope = rememberCoroutineScope()
    val passcodeManager = koinInject<PasscodeManager>()

    val startDestination by remember {
        mutableStateOf(
            if (isUsingPasscode) {
                Route.PasscodeScreen
            } else {
                Route.LoginScreen
            },
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeManager = passcodeManager,
                onPasscodeConfirm = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onForgotButton = {
                    navController.navigate(Route.LoginScreen) {
                        popUpTo(0)
                    }
                },
                onPasscodeCreation = {
                    navController.navigate(Route.BiometricSetupScreen)
                },
                onPasscodeChanged = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onEnableDisableBiometrics = {},
                onPasscodeRejected = {},
                onBiometricError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message ?: "Biometric authentication failed"
                },
                biometricButton = { modifier ->
                    BiometricKey(
                        modifier = modifier,
                        passcodeManager = passcodeManager,
                        onUserNotRegistered = {
                            dialogBoxType = DialogBoxType.ERROR
                            dialogMessage = "User not registered for biometrics. Please use passcode or re-register in settings."
                        }
                    )
                }
            )

            if (dialogBoxType != DialogBoxType.None) {
                MessageDiaglogBox(
                    onDismissRequest = {
                        dialogBoxType = DialogBoxType.None
                    },
                    dialogMessage = dialogMessage,
                )
            }
        }

        composable<Route.BiometricSetupScreen> {
            BiometricSetupScreen(
                onSuccess = {
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onSkip = {
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message
                }
            )

            if (dialogBoxType != DialogBoxType.None) {
                MessageDiaglogBox(
                    onDismissRequest = { dialogBoxType = DialogBoxType.None },
                    dialogMessage = dialogMessage,
                )
            }
        }

        composable<Route.SettingsPasscodeScreen> {
            PasscodeScreen(
                passcodeManager = passcodeManager,
                onPasscodeConfirm = {},
                onForgotButton = {
                    navController.navigate(Route.LoginScreen) {
                        popUpTo(0)
                    }
                },
                onPasscodeCreation = {},
                onPasscodeChanged = {},
                onEnableDisableBiometrics = {
                    passcodeManager.trySendAction(PasscodeAction.DeleteBiometricRegistration)
                    navController.popBackStack()
                },
                onPasscodeRejected = {},
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen {
                navController.navigate(Route.PasscodeScreen)
            }
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                navController = navController,
                usingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank(),
                onLogoutClick = {},
                changePasscode = {},
            )
        }
    }
}

@Composable
fun BiometricKey(
    modifier: Modifier,
    passcodeManager: PasscodeManager,
    onUserNotRegistered: () -> Unit,
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
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
                        passcodeStorageAdapter.loadRegistrationData() ?: ""
                    )
                    when (result) {
                        is AuthenticationResult.Success -> {
                            passcodeManager.trySendAction(PasscodeAction.BiometricUnlockSuccess)
                        }
                        is AuthenticationResult.Error -> {
                            passcodeManager.trySendAction(PasscodeAction.BiometricUnlockFailure(result.message))
                        }
                        is AuthenticationResult.UserNotRegistered -> {
                            onUserNotRegistered()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LoginScreen(
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Login Screen",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(100.dp))
        Button(
            onClick = {
                onLogoutClick()
            },
        ) {
            Text(
                "Setup App Lock",
            )
        }
    }
}

@Composable
fun HomeScreen(
    usingPasscode: Boolean,
    onLogoutClick: () -> Unit,
    changePasscode: () -> Unit,
    passcodeManager: PasscodeManager = koinInject<PasscodeManager>(),
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
    navController: NavHostController,
) {
    val state by passcodeManager.state.collectAsState()
    val platformAuthenticationProvider = platformAuthenticationProvider.current
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
                passcodeManager.trySendAction(PasscodeAction.LogOutErase)
                navController.navigate(Route.LoginScreen) {
                    popUpTo(0)
                }
                onLogoutClick()
            },
        ) {
            Text(
                "Log Out",
            )
        }

        if (usingPasscode) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    passcodeManager.trySendAction(PasscodeAction.ChangePasscode)
                    navController.navigate(Route.PasscodeScreen)
                    changePasscode()
                },
            ) {
                Text(
                    "Change Passcode",
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (state.isBiometricEnabled) {
                        // Disabling: requires passcode verification
                        passcodeManager.trySendAction(PasscodeAction.EnableDisableBiometrics)
                        navController.navigate(Route.SettingsPasscodeScreen)
                    } else {
                        // Enabling
                        if (state.isBiometricRegistered) {
                            // Already registered: verify with biometrics
                            scope.launch {
                                val result = platformAuthenticationProvider.onAuthenticatorClick(
                                    "Verify Biometrics to Enable",
                                    passcodeStorageAdapter.loadRegistrationData() ?: ""
                                )
                                when (result) {
                                    is AuthenticationResult.Success -> {
                                        // Since Enabled is tied to Registered, and we don't have separate toggle in manager logic
                                        // Wait, the manager logic I just wrote: SaveBiometricRegistration sets Enabled=true.
                                        // But if they just "Enabling" without re-registering? 
                                        // The user instruction was: "set the isEnabled to true when the biometrics data is saved for registration and set it to false when it is deleted."
                                        // So there is NO separate toggle anymore. 
                                        // If Registered, it IS Enabled.
                                    }
                                    AuthenticationResult.UserNotRegistered -> {
                                        navController.navigate(Route.BiometricSetupScreen)
                                    }
                                    else -> {}
                                }
                            }
                        } else {
                            // Not registered: go to setup
                            navController.navigate(Route.BiometricSetupScreen)
                        }
                    }
                },
            ) {
                Text(
                    if (state.isBiometricEnabled) "Disable Biometrics" else "Enable Biometrics",
                )
            }
        }
    }
}
