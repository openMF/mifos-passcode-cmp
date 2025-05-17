package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.LocalPlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.chooseAppLock.AppLockSaver
import com.mifos.passcode.auth.chooseAppLock.ChooseAuthOptionScreen
import com.mifos.passcode.auth.chooseAppLock.rememberAppLockSaver
import com.mifos.passcode.auth.deviceAuth.DeviceAuthScreen
import com.mifos.passcode.auth.passcode.rememberPasscodeSaver
import com.mifos.passcode.auth.passcode.screen.PasscodeScreen
import com.mifos.passcode.sample.authentication.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl


@Composable
fun PasscodeNavigation(){

    val preferenceDataStore = PreferenceDataStoreImpl()

    val navController = rememberNavController()

    val passcodeRepository = PasscodeRepository(preferenceDataStore)

    val chooseAuthOptionRepository = ChooseAuthOptionRepository(preferenceDataStore)


    val authOptionSaver = rememberAppLockSaver(
        currentAppLock = chooseAuthOptionRepository.getAuthOption(),
        setAppLock = {
            chooseAuthOptionRepository.setAuthOption(it)
        },
        clearAppLock = { chooseAuthOptionRepository.clearAuthOption() }
    )

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


    var startDestination: Route by remember {
        mutableStateOf(Route.LoginScreen)
    }

    startDestination = when(chooseAuthOptionRepository.getAuthOption()){
        AppLockSaver.AppLockOption.MifosPasscode -> {
            if(passcodeRepository.isPasscodeSet()){
                Route.PasscodeScreen
            }else {
                authOptionSaver.clearCurrentAppLock()
                Route.LoginScreen
            }
        }
        AppLockSaver.AppLockOption.DeviceLock -> {
            Route.DeviceAuthScreen
        }
        AppLockSaver.AppLockOption.None -> Route.LoginScreen
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){

        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                appLockSaver = authOptionSaver,
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
                    authOptionSaver.clearCurrentAppLock()
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
                authOptionSaver.clearCurrentAppLock()
                passcodeSaver.forgetPasscode()
                navController.navigate(Route.LoginScreen)
            }
        }

        composable<Route.DeviceAuthScreen> {
            DeviceAuthScreen(
                onDeviceAuthSuccess = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                },
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