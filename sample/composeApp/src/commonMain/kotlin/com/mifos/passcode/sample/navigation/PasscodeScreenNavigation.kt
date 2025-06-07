package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.LibraryLocalAndroidActivity
import com.mifos.passcode.Platform
import com.mifos.passcode.auth.chooseAppLock.AppLockSaver
import com.mifos.passcode.auth.chooseAppLock.ChooseAuthOptionScreen
import com.mifos.passcode.auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.auth.deviceAuth.DeviceAuthScreen
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.auth.passcode.rememberPasscodeSaver
import com.mifos.passcode.auth.passcode.screen.PasscodeScreen
import com.mifos.passcode.getPlatform
import com.mifos.passcode.sample.authentication.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


const val REGISTRATION_DATA = "REGISTRATION_DATA"
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun SampleAppNavigation(){

    val authenticator = PlatformAuthenticator(LibraryLocalAndroidActivity.current)

    val navController = rememberNavController()

    val kmpDataStore = PreferenceDataStoreImpl()

    val passcodeRepository = PasscodeRepository(kmpDataStore)

    val chooseAuthOptionRepository = ChooseAuthOptionRepository(kmpDataStore)
    val scope = rememberCoroutineScope()

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
                chooseAuthOptionRepository.clearAuthOption()
                Route.LoginScreen
            }
        }
        AppLockSaver.AppLockOption.DeviceLock -> {
            if(
                getPlatform() == Platform.JVM && kmpDataStore.getSavedData(REGISTRATION_DATA, "").isEmpty()
            ){
                chooseAuthOptionRepository.clearAuthOption()
                Route.LoginScreen
            }else{
                Route.DeviceAuthScreen
            }
        }
        AppLockSaver.AppLockOption.None -> Route.LoginScreen
    }


    val platformAuthenticationProvider = PlatformAuthenticationProvider(
        authenticator,
        scope
    )

    val platformAuthenticationResult by platformAuthenticationProvider.authenticationResult.collectAsState()


//    LaunchedEffect(platformAuthenticationResult){
//        val result = platformAuthenticationResult
//        when(result){
//            is AuthenticationResult.Error -> {}
//            is AuthenticationResult.Failed -> {}
//            is AuthenticationResult.RegisterAgain -> {}
//
//            null -> {}
//        }
//    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){

        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                whenDeviceLockSelected = {
                    scope.launch {
                        val authenticationResult = platformAuthenticationProvider.registerUser()

                        if ( authenticationResult.first is AuthenticationResult.Success) {
                            if(getPlatform() == Platform.JVM) {
                                kmpDataStore.putData(REGISTRATION_DATA, authenticationResult.second)
                                chooseAuthOptionRepository.setAuthOption(AppLockSaver.AppLockOption.DeviceLock)

                                println("Saved Data: ${kmpDataStore.getSavedData(REGISTRATION_DATA, "")}")
                                println("Saved app lock: ${chooseAuthOptionRepository.getAuthOption()}")
                                navController.popBackStack()
                                navController.navigate(Route.HomeScreen){
                                    popUpTo(0)
                                }
                            }else{
                                chooseAuthOptionRepository.setAuthOption(AppLockSaver.AppLockOption.DeviceLock)
                                println("Saved app lock: ${chooseAuthOptionRepository.getAuthOption()}")
                                navController.popBackStack()
                                navController.navigate(Route.HomeScreen){
                                    popUpTo(0)
                                }
                            }
                        }
                    }
                },
                whenPasscodeSelected = {
                    chooseAuthOptionRepository.setAuthOption(AppLockSaver.AppLockOption.MifosPasscode)
                    navController.popBackStack()
                    navController.navigate(route = Route.PasscodeScreen){
                        popUpTo(0)
                    }
                },
                loading = platformAuthenticationProvider.isLoading.value
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
            DeviceAuthScreen(
                onDeviceAuthSuccess = {
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen) {
                        popUpTo(0)
                    }
                },
                platformAuthenticationProvider = platformAuthenticationProvider,
                onClickAuthentication = {
                    scope.launch {
                        val savedData = if (getPlatform() == Platform.JVM) {
                            println("Entered savedData for JVM check")
                            kmpDataStore.getSavedData(REGISTRATION_DATA, "")
                        } else {
                            null
                        }
                        println("Saved data: $savedData")
                        val result = platformAuthenticationProvider.onAuthenticatorClick(
                            appName = "Mifos Authenticator",
                            savedRegistrationData = savedData // Assuming 'savedData' is available here
                        )
                        if (result is AuthenticationResult.Success) {
                            navController.popBackStack()
                            navController.navigate(Route.HomeScreen) {
                                popUpTo(0)
                            }
                        } else if (result is AuthenticationResult.Error) {

                        }
                    }
                },
                onDeviceAuthFailed = {},
                onDeviceAuthError = {},
                onRegisterAgainRequired = {},
                onLogout = {
                    kmpDataStore.clearData(REGISTRATION_DATA)
                    chooseAuthOptionRepository.clearAuthOption()
                    passcodeSaver.forgetPasscode()
                    navController.navigate(Route.LoginScreen)
                }
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