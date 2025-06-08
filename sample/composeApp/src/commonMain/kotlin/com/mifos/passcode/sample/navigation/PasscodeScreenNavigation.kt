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
import com.mifos.passcode.sample.authentication.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.AppLockOption
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.sample.chooseAuthOption.AppLockSaver
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreen
import com.mifos.passcode.sample.deviceAuth.PlatformAuthenticationScreen
import com.mifos.passcode.sample.deviceAuth.REGISTRATION_DATA
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SampleAppNavigation(){

    val navController = rememberNavController()

    val kmpDataStore = PreferenceDataStoreImpl()

    val passcodeRepository = PasscodeRepository(kmpDataStore)

    val chooseAuthOptionRepository = ChooseAuthOptionRepository(kmpDataStore)

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
            when(chooseAuthOptionRepository.getAuthOption()){
                AppLockOption.MifosPasscode -> {
                    if(passcodeRepository.isPasscodeSet()){
                        Route.PasscodeScreen
                    }else {
                        chooseAuthOptionRepository.clearAuthOption()
                        Route.LoginScreen
                    }
                }
                AppLockOption.DeviceLock -> {
                    if(
                        getPlatform() == Platform.JVM && kmpDataStore.getSavedData(REGISTRATION_DATA, "").isEmpty()
                    ){
                        chooseAuthOptionRepository.clearAuthOption()
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
                koinViewModel(),
                whenPasscodeSelected = {},
                whenDeviceLockSelected = {},
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
                    chooseAuthOptionRepository.clearAuthOption()
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
                chooseAuthOptionRepository.clearAuthOption()
                kmpDataStore.clearData(REGISTRATION_DATA)
                passcodeSaver.forgetPasscode()
                navController.navigate(Route.LoginScreen)
            }
        }

        composable<Route.DeviceAuthScreen> {
            PlatformAuthenticationScreen(
                koinViewModel(),
                navController = navController,
                onClickAuthentication = {},
                onLogout = {}
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