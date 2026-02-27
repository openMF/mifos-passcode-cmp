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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmp.sample.shared.chooseAuthOption.AppLockOption
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionRepository
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreen
import cmp.sample.shared.platformAuthentication.AuthenticationScreen
import org.koin.compose.koinInject
import org.mifos.authenticator.biometrics.Platform
import org.mifos.authenticator.biometrics.getPlatform
import org.mifos.authenticator.passcode.PasscodeAction
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStorageAdapter
import org.mifos.authenticator.passcode.screen.PasscodeScreen

@Composable
fun SampleAppNavigation(
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
    chooseAuthOptionRepository: ChooseAuthOptionRepository = koinInject(),
) {
    val navController = rememberNavController()

    val currentAppLock = chooseAuthOptionRepository.getAuthOption()

    val isUsingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank()

    val startDestination by remember {
        mutableStateOf(
            when (currentAppLock) {
                AppLockOption.MifosPasscode -> {
                    if (isUsingPasscode) {
                        Route.PasscodeScreen
                    } else {
                        chooseAuthOptionRepository.clearAuthOption()
                        Route.LoginScreen
                    }
                }
                AppLockOption.DeviceLock -> {
                    if (
                        getPlatform() == Platform.JVM &&
                        chooseAuthOptionRepository.getRegistrationData().isEmpty()
                    ) {
                        chooseAuthOptionRepository.clearAuthOption()
                        Route.LoginScreen
                    } else {
                        Route.DeviceAuthScreen
                    }
                }

                AppLockOption.None -> Route.LoginScreen
            },
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                navController = navController,
            )
        }

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeManager = koinInject<PasscodeManager>(),
                onPasscodeConfirm = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onPasscodeSkipped = {
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
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onPasscodeRejected = {},
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen {
                navController.navigate(Route.ChooseAuthOptionScreen)
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

        composable<Route.DeviceAuthScreen> {
            AuthenticationScreen(
                navController = navController,
            )
        }
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
    chooseAuthOptionRepository: ChooseAuthOptionRepository = koinInject(),
    passcodeManager: PasscodeManager = koinInject<PasscodeManager>(),
    passcodeStorageAdapter: PasscodeStorageAdapter = koinInject(),
    navController: NavHostController,
) {
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
                chooseAuthOptionRepository.clearAuthOption()
                chooseAuthOptionRepository.clearRegistrationData()
                passcodeManager.trySendAction(PasscodeAction.LogOutErasePasscode)
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
        }
    }
}
