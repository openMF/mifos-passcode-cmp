package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mifos.passcode.LibraryLocalAndroidActivity
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
import kotlinx.coroutines.launch


const val REGISTRATION_DATA = "REGISTRATION_DATA"
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
                kmpDataStore.clearData(REGISTRATION_DATA)
                Route.LoginScreen
            }
        }
        AppLockSaver.AppLockOption.DeviceLock -> {
            if(
                getPlatform().name.contains("Java", true) && kmpDataStore.getSavedData(REGISTRATION_DATA, "").isEmpty()
            ){
                chooseAuthOptionRepository.clearAuthOption()
                Route.LoginScreen
            }else{
                Route.DeviceAuthScreen
            }
        }
        AppLockSaver.AppLockOption.None -> Route.LoginScreen
    }

    val savedData = rememberSaveable{
        mutableStateOf(kmpDataStore.getSavedData(REGISTRATION_DATA, ""))
    }

    val platformAuthenticationProvider = PlatformAuthenticationProvider(
        savedRegistrationData = savedData.value,
        authenticator,
        scope
    )

    val platformAuthenticationResult by platformAuthenticationProvider.authenticationResult.collectAsState()

//    LaunchedEffect(platformAuthenticationResult){
//        when(platformAuthenticationResult){
//            is AuthenticationResult.Error -> {}
//            is AuthenticationResult.Failed -> {}
//            is AuthenticationResult.RegisterAgain -> {}
//            is AuthenticationResult.Success -> {
//                println("Authentication Success")
//
//            }
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
                        platformAuthenticationProvider.registerUser()

                        if(platformAuthenticationResult is AuthenticationResult.Success){
                            if(getPlatform().name.contains("Java", true)) {
                                kmpDataStore.putData(REGISTRATION_DATA, (platformAuthenticationResult as AuthenticationResult.Success).message)
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
                        savedData.value = kmpDataStore.getSavedData(REGISTRATION_DATA, "")
                        platformAuthenticationProvider.onAuthenticatorClick("Mifos Authenticator")
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