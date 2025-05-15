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
import com.mifos.passcode.LocalAuthOption
import com.mifos.passcode.auth.chooseAppLock.AuthOptionSaver
import com.mifos.passcode.auth.chooseAppLock.ChooseAuthOptionScreen
import com.mifos.passcode.auth.chooseAppLock.rememberAuthOptionSaver
import com.mifos.passcode.auth.passcode.PasscodeViewModel
import com.mifos.passcode.auth.passcode.screen.PasscodeScreen
import com.mifos.passcode.sample.authentication.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.chooseAuthOption.utils.Helpers
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.ui.screen.DeviceAuthScreen
import kotlinx.coroutines.flow.MutableStateFlow
import com.mifos.passcode.sample.utils.Constants

@Composable
fun SampleAppNavigation(){

    val preferenceDataStore = PreferenceDataStoreImpl()
    val navController = rememberNavController()
    val passcodeRepository = PasscodeRepository(preferenceDataStore)
    val chooseAuthOptionRepository = ChooseAuthOptionRepository(preferenceDataStore)
    val availableAuthOptions = LocalAuthOption.current

    val authOptionSaver: AuthOptionSaver = rememberAuthOptionSaver(
        currentAppLock = chooseAuthOptionRepository.getAuthOption(),
        setAuthOption = {
            chooseAuthOptionRepository.setAuthOption(it)
        },
        clearAuthOption = { chooseAuthOptionRepository.clearAuthOption() }
    )

    val passcodeViewModel = PasscodeViewModel(
        currentPasscode = passcodeRepository.getPasscode(),
        isPasscodeSet = passcodeRepository.isPasscodeSet(),
        savePasscode = {
            passcodeRepository.savePasscode(it)
        },
        clearPasscode = {
            passcodeRepository.clearPasscode()
        },
    )



    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ){

        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                authOption = availableAuthOptions,
                authOptionSaver = rememberAuthOptionSaver(
                    currentAppLock = chooseAuthOptionRepository.getAuthOption(),
                    setAuthOption = {
                        chooseAuthOptionRepository.setAuthOption(it)
                    },
                    clearAuthOption = { chooseAuthOptionRepository.clearAuthOption() }
                ),
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
                chooseAuthOptionRepository
            )
        }

        composable<Route.HomeScreen> {
            HomeScreen(
                navController = navController,
                passcodeRepository = passcodeRepository,
                chooseAuthOptionRepository
            )
        }

        composable<Route.DeviceAuthScreen> {
            DeviceAuthScreen(
                authOption = availableAuthOptions,
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
    chooseAuthOptionRepository: ChooseAuthOptionRepository
){
    val currentAppLock = MutableStateFlow(
        Helpers.authOptionToStringMapperFunction(chooseAuthOptionRepository.getAuthOption())
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
    passcodeRepository: PasscodeRepository,
    chooseAuthOptionRepository: ChooseAuthOptionRepository
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
                chooseAuthOptionRepository.clearAuthOption()
                navController.navigate(Route.LoginScreen)
            }
        ) {
            Text(
                "Log Out"
            )
        }
    }
}