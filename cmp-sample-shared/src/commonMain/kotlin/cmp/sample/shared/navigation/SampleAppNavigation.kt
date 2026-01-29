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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmp.sample.shared.chooseAuthOption.AppLockOption
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreen
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import cmp.sample.shared.passcode.PasscodeRepository
import cmp.sample.shared.platformAuthentication.AuthenticationScreen
import cmp.sample.shared.platformAuthentication.AuthenticationScreenViewModel
import org.mifos.authenticator.biometrics.Platform
import org.mifos.authenticator.biometrics.getPlatform
import org.mifos.authenticator.passcode.rememberPasscodeSaver
import org.mifos.authenticator.passcode.screen.PasscodeScreen

@Composable
fun SampleAppNavigation(
    passcodeRepository: PasscodeRepository,
    chooseAuthOptionScreenViewmodel: ChooseAuthOptionScreenViewmodel,
    platformAuthOptionScreenViewmodel: AuthenticationScreenViewModel,
) {
    val navController = rememberNavController()

    val currentAppLock = chooseAuthOptionScreenViewmodel.getAppLock()

    var isPasscodeSet by rememberSaveable {
        mutableStateOf(passcodeRepository.isPasscodeSet())
    }
    var savedPasscode by rememberSaveable {
        mutableStateOf(passcodeRepository.getPasscode())
    }

    val passcodeSaver = rememberPasscodeSaver(
        currentPasscode = savedPasscode,
        isPasscodeSet = isPasscodeSet,
        saveNewPasscode = { passcode ->
            passcodeRepository.savePasscode(passcode)
        },
        clearCurrentPasscode = {
            passcodeRepository.clearPasscode()
        },
        currentPasscodeLength = passcodeRepository.getPasscodeLength(),
        savePasscodeLength = { passcodeLength ->
            passcodeRepository.savePasscodeLength(passcodeLength.length)
        },
    )

    val startDestination by remember {
        mutableStateOf(
            when (currentAppLock) {
                AppLockOption.MifosPasscode -> {
                    if (passcodeRepository.isPasscodeSet()) {
                        Route.PasscodeScreen
                    } else {
                        chooseAuthOptionScreenViewmodel.clearAppLock()
                        passcodeRepository.clearPasscodeLength()
                        Route.LoginScreen
                    }
                }
                AppLockOption.DeviceLock -> {
                    if (
                        getPlatform() == Platform.JVM &&
                        chooseAuthOptionScreenViewmodel.getRegistrationData().isEmpty()
                    ) {
                        chooseAuthOptionScreenViewmodel.clearAppLock()
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
                chooseAuthOptionScreenViewmodel,
                navController = navController,
            )
        }

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeSaver = passcodeSaver,
                onPasscodeConfirm = {
                    passcodeRepository.savePasscode(
                        it,
                    )
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onForgotButton = {
                    passcodeSaver.forgetPasscode()
                    savedPasscode = passcodeRepository.getPasscode()
                    isPasscodeSet = passcodeRepository.isPasscodeSet()
                    navController.navigate(Route.LoginScreen) {
                        popUpTo(0)
                    }
                },
                onSkipButton = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen {
                navController.navigate(Route.ChooseAuthOptionScreen)
            }
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                usingPasscode = passcodeRepository.isPasscodeSet(),
                onLogoutClick = {
                    chooseAuthOptionScreenViewmodel.clearAppLock()
                    chooseAuthOptionScreenViewmodel.clearRegistrationData()
                    passcodeSaver.forgetPasscode()
                    savedPasscode = passcodeRepository.getPasscode()
                    isPasscodeSet = passcodeRepository.isPasscodeSet()
                    navController.navigate(Route.LoginScreen) {
                        popUpTo(0)
                    }
                },
                resetPasscode = {
                    passcodeSaver.resetPasscode()
                    navController.navigate(Route.PasscodeScreen)
                }
            )
        }

        composable<Route.DeviceAuthScreen> {
            AuthenticationScreen(
                platformAuthOptionScreenViewmodel,
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
    usingPasscode: Boolean = false,
    onLogoutClick: () -> Unit,
    resetPasscode: () -> Unit = {},
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
                onLogoutClick()
            },
        ) {
            Text(
                "Log Out",
            )
        }

        if(usingPasscode) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    resetPasscode()
                },
            ) {
                Text(
                    "Change Passcode",
                )
            }
        }

    }
}
