package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.ui.screen.ChooseAuthOptionScreen
import com.mifos.passcode.ui.screen.DeviceAuthScreen
import com.mifos.passcode.ui.screen.PasscodeScreen
import com.mifos.passcode.ui.viewmodels.ChooseAuthOptionViewModel
import com.mifos.passcode.ui.viewmodels.PasscodeViewModel
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel
import com.mifos.passcode.utility.Constants
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun PasscodeNavigation(){

    val navController = rememberNavController()
    val passcodeViewModel: PasscodeViewModel = koinViewModel()
    val deviceAuthenticatorViewModel: DeviceAuthenticatorViewModel = koinViewModel()
    val  chooseAuthOptionViewModel: ChooseAuthOptionViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ){

        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                authOption = koinInject(),
                chooseAuthOptionViewModel,
                whenDeviceLockSelected = {
                    navController.popBackStack()
                    navController.navigate(route = Route.DeviceAuthScreen){
                        popUpTo(0)
                    }
                },
                whenPasscodeSelected = {
                    navController.popBackStack()
                    navController.navigate(route = Route.PasscodeScreen){
                        popUpTo(0)
                    }
                }
            )
        }

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeViewModel = passcodeViewModel,
                onSkipButton = {
                    navController.popBackStack()
                    navController.navigate(route = Route.HomeScreen,){
                        popUpTo(0)
                    }
                },
                onPasscodeConfirm = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                },
                onPasscodeRejected = {
                    passcodeViewModel.restart()
                },
                onForgotButton = {
                    passcodeViewModel.forgetPasscode()
                    navController.navigate(Route.LoginScreen){
                        popUpTo(0)
                    }
                    chooseAuthOptionViewModel.clearAppLock()
                }
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen(
                navController = navController,
                chooseAuthOptionViewModel
            )
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                navController = navController,
                passcodeViewModel = passcodeViewModel,
                chooseAuthOptionViewModel
            )
        }

        composable<Route.DeviceAuthScreen> {
            DeviceAuthScreen(
                authOption = koinInject(),
                onDeviceAuthSuccess = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                },
                deviceAuthenticatorViewModel = deviceAuthenticatorViewModel,
            )
        }
    }
}


@Composable
fun LoginScreen(
    navController: NavController,
    chooseAuthOptionViewModel: ChooseAuthOptionViewModel
){
    val currentAppLock = chooseAuthOptionViewModel.currentAppLock

    if(currentAppLock.value == Constants.DEVICE_AUTHENTICATION_METHOD_VALUE ||
        currentAppLock.value == Constants.MIFOS_PASSCODE_VALUE
    ){
        navController.navigate(Route.ChooseAuthOptionScreen)
    }else {
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
                    navController.navigate(Route.ChooseAuthOptionScreen)
                }
            ) {

                Text(
                    "Create Passcode"
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    passcodeViewModel: PasscodeViewModel,
    chooseAuthOptionViewModel: ChooseAuthOptionViewModel
){
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
                passcodeViewModel.forgetPasscode()
                chooseAuthOptionViewModel.clearAppLock()
                navController.navigate(Route.LoginScreen)
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}