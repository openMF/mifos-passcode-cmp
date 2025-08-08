package org.mifos.authenticator.sample.navigation

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
import org.mifos.authenticator.biometrics.Platform
import org.mifos.authenticator.biometrics.getPlatform
import org.mifos.authenticator.sample.chooseAuthOption.AppLockOption
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionScreen
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import org.mifos.authenticator.sample.passcode.PasscodeRepository
import org.mifos.authenticator.sample.platformAuthentication.AuthenticationScreen
import org.mifos.authenticator.sample.platformAuthentication.AuthenticationScreenViewModel
import org.mifos.authenticator.passcode.rememberPasscodeSaver
import org.mifos.authenticator.passcode.screen.PasscodeScreen
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionRepository
import org.mifos.authenticator.sample.kmpDataStore.PreferenceDataStore


@Composable
fun SampleAppNavigation(
    passcodeRepository: PasscodeRepository,
    chooseAuthOptionRepository: ChooseAuthOptionRepository,
    preferenceDataStore: PreferenceDataStore
) {
    val navController = rememberNavController()

    var isPasscodeSet by rememberSaveable {
        mutableStateOf(passcodeRepository.isPasscodeSet())
    }
    var savedPasscode by rememberSaveable {
        mutableStateOf(passcodeRepository.getPasscode())
    }

    val passcodeSaver = rememberPasscodeSaver(
        currentPasscode = savedPasscode,
        isPasscodeSet = isPasscodeSet,
        savePasscode = { passcode ->
            passcodeRepository.savePasscode(passcode)
        },
        clearPasscode = {
            passcodeRepository.clearPasscode()
        },
        currentPasscodeLength = passcodeRepository.getPasscodeLength(),
        savePasscodeLength = {passcodeLength ->
            passcodeRepository.savePasscodeLength(passcodeLength.length)
        }
    )

    val startDestination by remember {
        mutableStateOf(
            when (chooseAuthOptionRepository.getAuthOption()) {
                AppLockOption.MifosPasscode -> {
                    if (passcodeRepository.isPasscodeSet()) {
                        Route.PasscodeScreen
                    } else {
                        chooseAuthOptionRepository.clearAuthOption()
                        passcodeRepository.clearPasscodeLength()
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
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.ChooseAuthOptionScreen> {

            val viewModel = ChooseAuthOptionScreenViewmodel(chooseAuthOptionRepository)

            ChooseAuthOptionScreen(
                viewModel,
                onNavigateToHomeScreen = {
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                },
                onNavigateToPasscodeScreen = {
                    navController.navigate(Route.PasscodeScreen)
                }
            )
        }

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeSaver = passcodeSaver,
                onPasscodeConfirm = {
                    passcodeRepository.savePasscode(
                        it
                    )
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
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen {
                navController.navigate(Route.ChooseAuthOptionScreen)
            }
        }

        composable<Route.HomeScreen> {
            HomeScreen {
                chooseAuthOptionRepository.clearAuthOption()
                chooseAuthOptionRepository.clearRegistrationData()
                passcodeSaver.forgetPasscode()
                savedPasscode = passcodeRepository.getPasscode()
                isPasscodeSet = passcodeRepository.isPasscodeSet()
                navController.navigate(Route.LoginScreen)
                navController.popBackStack<Route.LoginScreen>(false)

            }
        }

        composable<Route.DeviceAuthScreen> {backStack ->

            val viewModel = AuthenticationScreenViewModel(
                chooseAuthOptionRepository,
                preferenceDataStore
            )

            AuthenticationScreen(
                viewModel,
                onNavigateToHomeScreen = {
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                onNavigateToLoginScreen = {
                    navController.navigate(Route.LoginScreen)
                    navController.popBackStack<Route.LoginScreen>(false)
                }
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Login Screen",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(100.dp))
        Button(
            onClick = {
                onLogoutClick()
            }
        ) {
            Text(
                "Setup App Lock"
            )
        }
    }
}

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Home Screen",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {
                onLogoutClick()
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}
