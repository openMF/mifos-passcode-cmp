package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.ui.screen.PasscodeScreen
import com.mifos.passcode.ui.viewmodels.PasscodeViewModel
import com.mifos.passcode.ui.viewmodels.PlatformAuthenticatorViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun PasscodeNavigation(
    passcodeViewModel: PasscodeViewModel = koinViewModel<PasscodeViewModel>(),
    platformAuthenticatorViewModel: PlatformAuthenticatorViewModel = koinViewModel(),
){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ){

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeViewModel = passcodeViewModel,
                platformAuthenticatorViewModel = platformAuthenticatorViewModel,
                onSkipButton = {
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
                },
                onBiometricAuthSuccess = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                },
                enableSystemAuthentication = true,
                authOption = koinInject()
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen(
                navController = navController,
                passcodeViewModel = passcodeViewModel
            )
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                navController = navController,
                passcodeViewModel = passcodeViewModel
            )
        }

    }
}


@Composable
fun LoginScreen(
    navController: NavController,
    passcodeViewModel: PasscodeViewModel
){
    val isPasscodeAlreadySet = passcodeViewModel.isPasscodeAlreadySet.collectAsState()

    if(isPasscodeAlreadySet.value){
        navController.navigate(Route.PasscodeScreen)
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
                    navController.navigate(Route.PasscodeScreen)
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
    passcodeViewModel: PasscodeViewModel
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
                navController.navigate(Route.LoginScreen)
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}