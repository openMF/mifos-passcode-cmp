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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmp.sample.shared.platformAuthentication.BiometricSetupScreen
import cmp.sample.shared.platformAuthentication.PasscodeScreenWithBiometrics
import cmp.sample.shared.screens.HomeScreen
import cmp.sample.shared.screens.LoginScreen
import cmp.sample.shared.ui.components.DialogBoxType
import cmp.sample.shared.ui.components.MessageDialogBox
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

@Composable
fun SampleAppNavigation(
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
) {
    val navController = rememberNavController()
    val passcodeManager = koinInject<PasscodeManager>()
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val scope = rememberCoroutineScope()

    val isUsingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank()

    var dialogBoxType by remember { mutableStateOf(DialogBoxType.None) }
    var dialogMessage by remember { mutableStateOf("") }

    val startDestination by remember {
        mutableStateOf(
            if (isUsingPasscode) Route.PasscodeScreen else Route.LoginScreen,
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Route.PasscodeScreen> {
            PasscodeScreenWithBiometrics(
                passcodeManager = passcodeManager,
                onPasscodeResult = { result ->
                    when (result) {
                        PasscodeResult.Verified -> {
                            navController.popBackStack()
                            navController.navigate(Route.HomeScreen) { popUpTo(0) }
                        }
                        PasscodeResult.Created -> {
                            navController.navigate(Route.BiometricSetupScreen)
                        }
                        PasscodeResult.Changed -> {
                            navController.navigate(Route.HomeScreen) { popUpTo(0) }
                        }
                        PasscodeResult.Forgotten -> {
                            Logger.e { "Forget passcode is triggered" }
                            scope.launch { platformAuthenticationProvider.unregister() }
                            navController.navigate(Route.LoginScreen) { popUpTo(0) }
                        }
                        PasscodeResult.Rejected -> { }
                    }
                },
                onBiometricSuccess = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) { popUpTo(0) }
                },
                onBiometricError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message
                },
            )

            if (dialogBoxType != DialogBoxType.None) {
                MessageDialogBox(
                    onDismissRequest = { dialogBoxType = DialogBoxType.None },
                    dialogMessage = dialogMessage,
                )
            }
        }

        composable<Route.DisableBiometricVerify> {
            PasscodeScreenWithBiometrics(
                passcodeManager = passcodeManager,
                hideBiometricButton = true,
                onPasscodeResult = { result ->
                    when (result) {
                        PasscodeResult.Verified -> {
                            scope.launch { platformAuthenticationProvider.unregister() }
                            navController.popBackStack()
                        }
                        PasscodeResult.Forgotten -> {
                            Logger.e { "Forget passcode is triggered" }
                            scope.launch { platformAuthenticationProvider.unregister() }
                            navController.navigate(Route.LoginScreen) { popUpTo(0) }
                        }
                        PasscodeResult.Rejected -> { }
                        PasscodeResult.Created -> navController.popBackStack()
                        PasscodeResult.Changed -> navController.popBackStack()
                    }
                },
                onBiometricSuccess = { /* unreachable — hideBiometricButton = true */ },
                onBiometricError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message
                },
            )

            if (dialogBoxType != DialogBoxType.None) {
                MessageDialogBox(
                    onDismissRequest = { dialogBoxType = DialogBoxType.None },
                    dialogMessage = dialogMessage,
                )
            }
        }

        composable<Route.BiometricSetupScreen> {
            BiometricSetupScreen(
                onBiometricsRegistrationSuccess = {
                    navController.navigate(Route.HomeScreen) { popUpTo(0) }
                },
                onSkipBiometricSetup = {
                    navController.navigate(Route.HomeScreen) { popUpTo(0) }
                },
                onError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message
                },
            )

            if (dialogBoxType != DialogBoxType.None) {
                MessageDialogBox(
                    onDismissRequest = { dialogBoxType = DialogBoxType.None },
                    dialogMessage = dialogMessage,
                )
            }
        }

        composable<Route.LoginScreen> {
            LoginScreen(
                onSetupAppLock = { navController.navigate(Route.PasscodeScreen) },
            )
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                usingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank(),
                onLogoutClick = {
                    navController.navigate(Route.LoginScreen) { popUpTo(0) }
                },
                navigateToPasscodeScreen = {
                    navController.navigate(Route.PasscodeScreen)
                },
                navigateToDisableBiometricVerify = {
                    navController.navigate(Route.DisableBiometricVerify)
                },
                onBiometricsEnableError = { message ->
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = message
                },
            )
        }
    }
}
