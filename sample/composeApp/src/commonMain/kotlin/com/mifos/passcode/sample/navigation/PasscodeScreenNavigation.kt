package com.mifos.passcode.sample.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.Platform
import com.mifos.passcode.auth.passcode.rememberPasscodeSaver
import com.mifos.passcode.auth.passcode.screen.PasscodeScreen
import com.mifos.passcode.getPlatform
import com.mifos.passcode.sample.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.AppLockOption
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreen
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import com.mifos.passcode.sample.platformAuthentication.PlatformAuthenticationScreen
import com.mifos.passcode.sample.platformAuthentication.PlatformAuthenticationScreenViewModel


@Composable
fun SampleAppNavigation(
    passcodeRepository: PasscodeRepository,
    chooseAuthOptionScreenViewmodel: ChooseAuthOptionScreenViewmodel,
    platformAuthOptionScreenViewmodel: PlatformAuthenticationScreenViewModel
) {

    val navController = rememberNavController()

    val currentAppLock = chooseAuthOptionScreenViewmodel.getAppLock()


    val passcodeSaver = rememberPasscodeSaver(
        currentPasscode = passcodeRepository.getPasscode(),
        isPasscodeSet = passcodeRepository.isPasscodeSet(),
        savePasscode = {passcode ->
            passcodeRepository.savePasscode(passcode)
        },
        clearPasscode = {
            passcodeRepository.clearPasscode()
        }
    )


    val startDestination by  remember {
        mutableStateOf(
            when(currentAppLock){
                AppLockOption.MifosPasscode -> {
                    if(passcodeRepository.isPasscodeSet()){
                        Route.PasscodeScreen
                    }else {
                        chooseAuthOptionScreenViewmodel.clearAppLock()
                        Route.LoginScreen
                    }
                }
                AppLockOption.DeviceLock -> {
                    if(
                        getPlatform() == Platform.JVM && chooseAuthOptionScreenViewmodel.getRegistrationData().isEmpty()
                    ){
                        chooseAuthOptionScreenViewmodel.clearAppLock()
                        Route.LoginScreen
                    }else{
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
    ){
        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                chooseAuthOptionScreenViewmodel,
                navController = navController
            )
        }

        composable<Route.PasscodeScreen> {
            PasscodeScreen(
                passcodeSaver = passcodeSaver,
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
                onForgotButton = {
                    passcodeSaver.forgetPasscode()
                    navController.navigate(Route.LoginScreen){
                        popUpTo(0)
                    }
                }
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen{
                navController.navigate(Route.ChooseAuthOptionScreen)
            }
        }

        composable<Route.HomeScreen> {
            HomeScreen{
                chooseAuthOptionScreenViewmodel.clearAppLock()
                chooseAuthOptionScreenViewmodel.clearRegistrationData()
                passcodeSaver.forgetPasscode()
                navController.navigate(Route.LoginScreen)
            }
        }

        composable<Route.DeviceAuthScreen> {
            PlatformAuthenticationScreen(
                platformAuthOptionScreenViewmodel,
                navController = navController,
            )
        }
    }
}


@Composable
fun LoginScreen(
    onLogoutClick: () -> Unit,
){
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
                "Create Passcode"
            )
        }
    }
}

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit
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
                onLogoutClick()
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}