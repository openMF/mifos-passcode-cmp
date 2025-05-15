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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.LocalPlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.chooseAppLock.AuthOptionSaver
import com.mifos.passcode.auth.chooseAppLock.ChooseAuthOptionScreen
import com.mifos.passcode.auth.chooseAppLock.rememberAuthOptionSaver
import com.mifos.passcode.auth.passcode.rememberPasscodeSaver
import com.mifos.passcode.auth.passcode.screen.PasscodeScreen
import com.mifos.passcode.sample.authentication.passcode.PasscodeViewmodel
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionViewmodel
import com.mifos.passcode.sample.chooseAuthOption.utils.Constants
import com.mifos.passcode.sample.chooseAuthOption.utils.Helpers
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.auth.deviceAuth.DeviceAuthScreen
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun PasscodeNavigation(){

    val preferenceDataStore = PreferenceDataStoreImpl()
    val navController = rememberNavController()
    val passcodeViewmodel = PasscodeViewmodel(preferenceDataStore)
    val chooseAuthOptionViewmodel = ChooseAuthOptionViewmodel(preferenceDataStore)
    val availableAuthOptions = LocalPlatformAvailableAuthenticationOption.current

    val currentPasscode by passcodeViewmodel.currentPasscode.collectAsState()

    val currentSelectedAppLock by
        chooseAuthOptionViewmodel.currentSelectedAppLock.collectAsState()

    val authOptionSaver: AuthOptionSaver = rememberAuthOptionSaver(
        currentAppLock = currentSelectedAppLock,
        setAuthOption = {
            chooseAuthOptionViewmodel.setAuthOption(it)
        },
        clearAuthOption = { chooseAuthOptionViewmodel.clearAuthOption() }
    )

    val passcodeSaver = rememberPasscodeSaver(
        currentPasscode = currentPasscode,
        isPasscodeSet = passcodeViewmodel.isPasscodeSet(),
        savePasscode = {passcode ->
            passcodeViewmodel.savePasscode(passcode)
        },
        clearPasscode = {
            passcodeViewmodel.clearPasscode()
        }
    )

    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ){

        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                platformAvailableAuthenticationOption = availableAuthOptions,
                authOptionSaver = authOptionSaver,
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
                    navController.navigate(Route.LoginScreen){
                        popUpTo(0)
                    }
                    authOptionSaver.clearAppLock()
                }
            )
        }

        composable<Route.LoginScreen> {
            LoginScreen(
                navController = navController,
                currentSelectedAppLock
            )
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                navController = navController,
                passcodeRepository = passcodeViewmodel,
                chooseAuthOptionViewmodel
            )
        }

        composable<Route.DeviceAuthScreen> {
            DeviceAuthScreen(
                platformAvailableAuthenticationOption = availableAuthOptions,
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
    navController: NavController,
    currentAppLock: AuthOptionSaver.AppLockOption
){
    val currentAppLock = MutableStateFlow(
        Helpers.authOptionToStringMapperFunction(currentAppLock)
    )

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
    passcodeRepository: PasscodeViewmodel,
    chooseAuthOptionViewmodel: ChooseAuthOptionViewmodel
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
                passcodeRepository.clearPasscode()
                chooseAuthOptionViewmodel.clearAuthOption()
                navController.navigate(Route.LoginScreen)
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}