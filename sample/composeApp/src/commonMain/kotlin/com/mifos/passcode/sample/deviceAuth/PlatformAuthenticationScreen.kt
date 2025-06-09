package com.mifos.passcode.sample.deviceAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticatorStatus
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.sample.chooseAuthOption.DialogBoxType
import com.mifos.passcode.sample.deviceAuth.components.SystemAuthenticatorButton
import com.mifos.passcode.sample.navigation.Route
import com.mifos.passcode.ui.theme.blueTint
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformAuthenticationScreen(
    platformAuthenticationScreenViewModel: PlatformAuthenticationScreenViewModel,
    navController: NavController,
    onClickAuthentication: () -> Unit,
    onLogout: () -> Unit = {},
){

    val verificationResult = platformAuthenticationScreenViewModel.authenticationResult.collectAsState()

    val authenticatorStatus = platformAuthenticationScreenViewModel.authenticatorStatus.collectAsStateWithLifecycle()

    val platformAuthOptions by platformAuthenticationScreenViewModel.availableAuthenticationOption.collectAsState(
        initial = listOf(PlatformAuthOptions.UserCredential)
    )

    var showComingSoonDialogBox by rememberSaveable{
        mutableStateOf(false)
    }

    var dialogBoxType by rememberSaveable{
        mutableStateOf(DialogBoxType.None)
    }

    var dialogMessage by rememberSaveable{
        mutableStateOf("")
    }

    LaunchedEffect(Unit){
        if(authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)){
            platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
            navController.popBackStack()
            navController.navigate(Route.LoginScreen){
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(verificationResult.value ){
        when(verificationResult.value){
            is AuthenticationResult.Error ->{
                dialogBoxType = DialogBoxType.ERROR
                dialogMessage = (verificationResult.value as AuthenticationResult.Error).message
            }
            AuthenticationResult.Success ->{
                navController.popBackStack()
                navController.navigate(Route.HomeScreen){
                    popUpTo(0)
                }
            }
            AuthenticationResult.UserNotRegistered -> {
                dialogBoxType = DialogBoxType.NOT_SET
                dialogMessage = "The user has changed authentication settings, register again."
            }
            null -> {}
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
                            onLogout()
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
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            SystemAuthenticatorButton(
                onClick = {
                    showComingSoonDialogBox = true
                    platformAuthenticationScreenViewModel.authenticateUser("Mifos App")
                    if(verificationResult.value is AuthenticationResult.UserNotRegistered){
                        platformAuthenticationScreenViewModel.clearUserRegistrationFromApp()
                        navController.popBackStack()
                        navController.navigate(Route.LoginScreen){
                            popUpTo(0)
                        }
                    }
                    onClickAuthentication()
                },
                platformAuthOptions = platformAuthOptions,
                authenticatorStatus = authenticatorStatus.value
            )
        }
    }
}