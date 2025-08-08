package org.mifos.authenticator.sample.platformAuthentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.mifos.authenticator.sample.chooseAuthOption.DialogBoxType
import org.mifos.authenticator.sample.chooseAuthOption.MessageDialogBox
import org.mifos.authenticator.biometrics.LibraryLocalPlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.LibraryPlatformAvailableAuthenticationOption
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.authenticator.passcode.components.MifosIcon
import org.mifos.authenticator.sample.navigation.Route
import org.mifos.authenticator.sample.platformAuthentication.components.PlatformAuthenticatorButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    authenticationScreenViewModel: AuthenticationScreenViewModel,
    onNavigateToLoginScreen: () -> Unit = {},
    onNavigateToHomeScreen: () -> Unit = {}
) {

    val state by authenticationScreenViewModel.state.collectAsState()

    val event by authenticationScreenViewModel.eventFLow.collectAsState(initial = null)


    val platformAvailableAuthenticationOption = LibraryPlatformAvailableAuthenticationOption.current
    val platformAuthOptions by platformAvailableAuthenticationOption.currentAuthOption.collectAsStateWithLifecycle()

    val platformAuthenticationProvider = LibraryLocalPlatformAuthenticationProvider.current
    val authenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsStateWithLifecycle()

    var dialogBoxType by rememberSaveable {
        mutableStateOf(DialogBoxType.None)
    }

    var dialogMessage by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(event) {
        when(event){
            AuthenticationResultScreenEvent.OnRegistrationDataCleared -> {
                onNavigateToLoginScreen()
            }
            null -> {}
        }
    }

    LaunchedEffect(
        state.authenticationResult,
    ) {
        when (state.authenticationResult) {
            is AuthenticationResult.Error -> {
                dialogBoxType = DialogBoxType.ERROR
                dialogMessage = (state.authenticationResult as AuthenticationResult.Error).message
            }
            is AuthenticationResult.Success -> {
                onNavigateToHomeScreen()
            }
            is AuthenticationResult.UserNotRegistered -> {
                dialogBoxType = DialogBoxType.NOT_SET
                dialogMessage = "The user has changed authentication settings, register again."
                authenticationScreenViewModel.handleAction(
                    AuthenticationResultScreenAction.OnClearUserRegistrationData
                )
                onNavigateToLoginScreen()
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
                            authenticationScreenViewModel.handleAction(
                                AuthenticationResultScreenAction.OnClearUserRegistrationData
                            )
                            onNavigateToLoginScreen()
                        },
                    ) { Text("Log out") }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            when (dialogBoxType) {
                DialogBoxType.ERROR -> {
                    MessageDialogBox(
                        onDismissRequest = { dialogBoxType = DialogBoxType.None },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.NOT_SET -> {
                    MessageDialogBox(
                        onDismissRequest = {
                            authenticationScreenViewModel.handleAction(
                                AuthenticationResultScreenAction.OnClearUserRegistrationData
                            )
                            onNavigateToLoginScreen()
                        },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.NOT_AVAILABLE -> {
                    MessageDialogBox(
                        onDismissRequest = { dialogBoxType = DialogBoxType.None },
                        dialogMessage = dialogMessage
                    )
                }
                DialogBoxType.None -> {}
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                PlatformAuthenticatorButton(
                    onClick = {
                        authenticationScreenViewModel.handleAction(
                            AuthenticationResultScreenAction.OnVerifyUser(
                                "Mifos App",
                                platformAuthenticationProvider
                            )
                        )
                    },
                    platformAuthOptions = platformAuthOptions,
                    authenticatorStatus = authenticatorStatus
                )
            }
        }
    }
}
