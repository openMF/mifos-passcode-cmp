package com.mifos.passcode.sample.platformAuthentication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.LibraryLocalPlatformAuthenticationProvider
import com.mifos.passcode.LibraryPlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticatorStatus
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.sample.chooseAuthOption.DialogBoxType
import com.mifos.passcode.sample.chooseAuthOption.MessageDiaglogBox
import com.mifos.passcode.sample.navigation.Route
import com.mifos.passcode.sample.platformAuthentication.components.SystemAuthenticatorButton
import com.mifos.passcode.ui.theme.blueTint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformAuthenticationScreen(
    platformAuthenticationScreenViewModel: PlatformAuthenticationScreenViewModel,
    navController: NavController,
){
    val verificationResult = platformAuthenticationScreenViewModel.authenticationResult.collectAsState()

    val platformAvailableAuthenticationOption = LibraryPlatformAvailableAuthenticationOption.current

    val platformAuthOptions by platformAvailableAuthenticationOption.currentAuthOption.collectAsState()

    println(platformAuthOptions)

    val platformAuthenticationProvider = LibraryLocalPlatformAuthenticationProvider.current

    val authenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsState()

    println("Authenticator status: $authenticatorStatus")

    val isLoading by platformAuthenticationScreenViewModel.isLoading.collectAsStateWithLifecycle()

    var dialogBoxType by rememberSaveable{
        mutableStateOf(DialogBoxType.None)
    }

    var dialogMessage by rememberSaveable{
        mutableStateOf("")
    }

    LaunchedEffect(Unit){
        platformAvailableAuthenticationOption.updateCurrentAuthOption()
    }

    LaunchedEffect(Unit){
        if(authenticatorStatus.contains(PlatformAuthenticatorStatus.NOT_SETUP)){
            platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
            navController.popBackStack()
            navController.navigate(Route.LoginScreen){
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(
        verificationResult.value,
    ){
        this.launch {
            when(verificationResult.value){
                is AuthenticationResult.Error ->{
                    dialogBoxType = DialogBoxType.ERROR
                    dialogMessage = (verificationResult.value as AuthenticationResult.Error).message
                    platformAuthenticationScreenViewModel.setAuthenticationResultNull()
                }
                is AuthenticationResult.Success ->{
                    navController.popBackStack()
                    navController.navigate(Route.HomeScreen){
                        popUpTo(0)
                    }
                    platformAuthenticationScreenViewModel.setAuthenticationResultNull()
                }
                is AuthenticationResult.UserNotRegistered -> {
                    dialogBoxType = DialogBoxType.NOT_SET
                    dialogMessage = "The user has changed authentication settings, register again."
                    platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
                    navController.popBackStack()
                    navController.navigate(Route.LoginScreen){
                        popUpTo(0)
                    }
                    platformAuthenticationScreenViewModel.setAuthenticationResultNull()
                }
                null -> {}
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                { Text("") },
                actions = {
                    Button(
                        onClick = {
                            platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
                            navController.popBackStack()
                            navController.navigate(Route.LoginScreen){
                                popUpTo(0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(blueTint)
                    ){ Text("Log out") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Green
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            when(dialogBoxType){
                DialogBoxType.ERROR -> {
                    MessageDiaglogBox(
                        onDismissRequest = {dialogBoxType = DialogBoxType.None },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.NOT_SET -> {
                    MessageDiaglogBox(
                        onDismissRequest = {
                            platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
                            navController.popBackStack()
                            navController.navigate(Route.LoginScreen){
                                popUpTo(0)
                            }
                        },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.NOT_AVAILABLE ->{
                    MessageDiaglogBox(
                        onDismissRequest = {dialogBoxType = DialogBoxType.None },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.None -> {}
            }

            if(isLoading){
                CircularProgressIndicator()
            }else{
                SystemAuthenticatorButton(
                    onClick = {
                        platformAuthenticationProvider.updateAuthenticatorStatus()
                        platformAuthenticationScreenViewModel.authenticateUser("Mifos App", platformAuthenticationProvider)
                    },
                    platformAuthOptions = platformAuthOptions,
                    authenticatorStatus = authenticatorStatus
                )
            }
        }
    }
}