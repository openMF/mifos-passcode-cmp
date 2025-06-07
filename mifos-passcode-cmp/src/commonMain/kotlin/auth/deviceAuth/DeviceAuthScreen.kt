package com.mifos.passcode.auth.deviceAuth

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.passcode.LibraryLocalContextProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.components.SystemAuthenticatorButton
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.auth.passcode.components.SystemAuthSetupConfirmDialog
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAuthScreen(
    platformAuthenticationProvider: PlatformAuthenticationProvider,
    onClickAuthentication: () -> Unit,
    onDeviceAuthFailed: (String) -> Unit = {},
    onDeviceAuthError: (String) -> Unit = {},
    onRegisterAgainRequired: suspend () -> Unit = {},
    onDeviceAuthSuccess: (String) -> Unit,
    onLogout: () -> Unit = {},
) {

    val scope = rememberCoroutineScope()

    val platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption? =
        PlatformAvailableAuthenticationOption(
            LibraryLocalContextProvider.current
        )

    val authenticationResult by platformAuthenticationProvider.authenticationResult.collectAsStateWithLifecycle()

    var showAuthPrompt by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(authenticationResult){
        when(authenticationResult){
            is AuthenticationResult.Error -> {
                onDeviceAuthError(
                    (authenticationResult as AuthenticationResult.Error).message
                )
            }
            is AuthenticationResult.Failed -> {
                onDeviceAuthFailed((authenticationResult as AuthenticationResult.Failed).message)
            }
            is AuthenticationResult.Success -> {
                onDeviceAuthSuccess((authenticationResult as AuthenticationResult.Success).message)
            }
            is AuthenticationResult.RegisterAgain -> {
                onRegisterAgainRequired()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                {Text(stringResource(Res.string.app_name))},
                actions = {
                    Button(
                        onClick = onLogout
                    ){Text("Log out")}
                }
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
                onClick = onClickAuthentication,
                platformAuthOptions =
                    platformAvailableAuthenticationOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = platformAuthenticationProvider.authenticatorStatus.value,
            )

            val showDialogBox =
                !isPlatformAuthenticatorSupportAvailable(platformAuthenticationProvider.authenticatorStatus.value) &&
                        showAuthPrompt

            if (showDialogBox) {
                when(platformAuthenticationProvider.authenticatorStatus.value){
                    is PlatformAuthenticatorStatus.WebAuthenticatorStatus -> {
                        Text("No Authenticator available")
                    }
                    is PlatformAuthenticatorStatus.DesktopAuthenticatorStatus.WindowsAuthenticatorStatus ->{
                        Text("Setup Windows Hello From settings")
                    }
                    is PlatformAuthenticatorStatus.MobileAuthenticatorStatus -> {

                        SystemAuthSetupConfirmDialog(
                            cancelSetup = {
                                showAuthPrompt = false
                            },
                            setSystemAuthentication = {
                                showAuthPrompt = false
                                platformAuthenticationProvider.setupDeviceAuthenticator()
                            }
                        )

                    }
                    is PlatformAuthenticatorStatus.UnsupportedPlatform -> {
                        Text("Unsupported platform")
                    }
                }
            }

        }
    }

}

