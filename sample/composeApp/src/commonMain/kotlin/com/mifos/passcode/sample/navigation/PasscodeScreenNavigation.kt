package com.mifos.passcode.sample.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.mifos.passcode.Platform
import com.mifos.passcode.auth.chooseAppLock.AppLockSaver
import com.mifos.passcode.auth.chooseAppLock.ChooseAuthOptionScreen
import com.mifos.passcode.auth.deviceAuth.DeviceAuthScreen
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.auth.deviceAuth.isPlatformAuthenticatorSupportAvailable
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

    val platformAuthenticationProvider = PlatformAuthenticationProvider(authenticator)

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val startDestination by  remember {
        mutableStateOf(
            when(chooseAuthOptionRepository.getAuthOption()){
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
        )
    }

    val authenticationResult= remember{
        mutableStateOf<Pair<AuthenticationResult?, String>>(
            Pair(null, "")
        )
    }

    val authenticatorStatus = platformAuthenticationProvider.authenticatorStatus.collectAsState()

    LaunchedEffect(authenticationResult.value.first){
        when(authenticationResult.value.first){
            is AuthenticationResult.Error -> {
                snackBarHostState.showSnackbar((authenticationResult.value.first as AuthenticationResult.Error).message)
            }
            is AuthenticationResult.Failed -> {
                snackBarHostState.showSnackbar((authenticationResult.value.first as AuthenticationResult.Failed).message)
            }
            is AuthenticationResult.PlatformAuthenticatorNotSet -> {
                if(getPlatform() == Platform.JVM){
                    snackBarHostState.showSnackbar("Setup Windows Hello from settings")
                }else if(getPlatform() == Platform.ANDROID){
                    platformAuthenticationProvider.setupDeviceAuthenticator()
                    authenticationResult.value  = Pair(null, "")
                    if(isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)){
                        navController.popBackStack()
                        navController.navigate(Route.HomeScreen){
                            popUpTo(0)
                        }
                    }
                }else{
                    snackBarHostState.showSnackbar("Unsupported platform.")
                }
            }
            is AuthenticationResult.RegisterAgain -> {
                snackBarHostState.showSnackbar("Logout and register again.", "Ok", true, SnackbarDuration.Long)
                chooseAuthOptionRepository.clearAuthOption()
                kmpDataStore.clearData(REGISTRATION_DATA)
                navController.popBackStack()
                navController.navigate(route = Route.LoginScreen){ popUpTo(0) }
                authenticationResult.value  = Pair(null, "")
            }
            is AuthenticationResult.Success -> {
                if(getPlatform() == Platform.JVM &&
                    kmpDataStore.getSavedData(REGISTRATION_DATA, "").isEmpty()
                ) {
                    kmpDataStore.putData(REGISTRATION_DATA, authenticationResult.value.second)
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
                authenticationResult.value  = Pair(null, "")
            }
            null -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable<Route.ChooseAuthOptionScreen> {
            ChooseAuthOptionScreen(
                whenDeviceLockSelected = {
                    scope.launch {
                        kmpDataStore.clearData(REGISTRATION_DATA)
                        authenticationResult.value = platformAuthenticationProvider.registerUser()
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
                platformAuthenticatorStatus = authenticatorStatus.value,
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
                            savedRegistrationData = savedData
                        )
                        authenticationResult.value = Pair(result, "")
                    }
                },
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