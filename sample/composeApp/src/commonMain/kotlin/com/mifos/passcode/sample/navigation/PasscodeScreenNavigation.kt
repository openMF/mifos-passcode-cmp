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
import com.mifos.passcode.component.PasscodeScreen
import com.mifos.passcode.sample.navigation.Route.PasscodeScreen
import com.mifos.passcode.viewmodels.BiometricAuthorizationViewModel
import com.mifos.passcode.viewmodels.PasscodeViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun PasscodeNavigation(
    passcodeViewModel: PasscodeViewModel = koinViewModel<PasscodeViewModel>(),
    biometricAuthorizationViewModel: BiometricAuthorizationViewModel = koinViewModel<BiometricAuthorizationViewModel>()
){

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ){
        composable<PasscodeScreen> {

            PasscodeScreen(
                viewModel = passcodeViewModel,
                biometricAuthorizationViewModel = biometricAuthorizationViewModel,
                onSkipButton = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen)
                },
                onPasscodeConfirm = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen)
                },
                onPasscodeRejected = {},
                onForgotButton = {
                    passcodeViewModel.restart()
                    navController.popBackStack()
                    navController.navigate(Route.LoginScreen)
                }
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
    if(passcodeViewModel.isPasscodeAlreadySet.value){
        navController.navigate(Route.PasscodeScreen)
    } else {
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
                passcodeViewModel.restart()
                navController.navigate(Route.LoginScreen)
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }

}