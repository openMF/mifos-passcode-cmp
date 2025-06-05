package com.mifos.passcode.auth.deviceAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.passcode.LocalContextProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.components.SystemAuthenticatorButton
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.auth.passcode.components.SystemAuthSetupConfirmDialog
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource


@Composable
fun DeviceAuthScreen(
    promptTitle: String = "",
    promptDescription: String = "",
    platformAuthenticationProvider: PlatformAuthenticationProvider,
    onDeviceAuthFailed: (String) -> Unit = {},
    onDeviceAuthError: (String) -> Unit = {},
    onDeviceAuthSuccess: (String) -> Unit
) {

//    val scope = rememberCoroutineScope()

//    val platformAuthenticationProvider = PlatformAuthenticationProvider(
//        LocalPlatformAuthenticator.current,
//
//    )
    val platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption? =
        PlatformAvailableAuthenticationOption(
            LocalContextProvider.current
        )

    val authenticationResult by platformAuthenticationProvider.authenticationResult.collectAsStateWithLifecycle()

    val platformAuthenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsStateWithLifecycle()

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
            null -> {}
        }
    }

    val appName = promptTitle.ifEmpty { stringResource(Res.string.app_name) }

    Scaffold {
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
                    platformAuthenticationProvider.getDeviceAuthenticatorStatus()
                    platformAuthenticationProvider.onAuthenticatorClick(appName)
                    showAuthPrompt = true
                },
                platformAuthOptions =
                    platformAvailableAuthenticationOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = platformAuthenticatorStatus!!,
            )

            when(platformAuthenticatorStatus){
                is PlatformAuthenticatorStatus.WebAuthenticatorStatus -> {
                    if(showAuthPrompt){
                        Text("No Authenticator available")
                    }
                }
                is PlatformAuthenticatorStatus.DesktopAuthenticatorStatus.WindowsAuthenticatorStatus ->{
                    if(showAuthPrompt){
                        Text("Setup Windows Hello From settings")
                    }
                }
                is PlatformAuthenticatorStatus.MobileAuthenticatorStatus -> {
                    if(
                        !(platformAuthenticatorStatus as PlatformAuthenticatorStatus.MobileAuthenticatorStatus).userCredentialSet
                        && showAuthPrompt){
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
                }
                is PlatformAuthenticatorStatus.UnsupportedPlatform -> {
                    Text("Unsupported platform")
                }
                null -> {
                    Text("Unsupported platform")
                }
            }


        }
    }

}

