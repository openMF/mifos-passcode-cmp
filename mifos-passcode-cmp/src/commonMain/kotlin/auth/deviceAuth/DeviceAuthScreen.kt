package com.mifos.passcode.auth.deviceAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
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
import com.mifos.passcode.LocalPlatformAuthenticator
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.passcode.components.MifosIcon
import com.mifos.passcode.auth.passcode.components.SystemAuthSetupConfirmDialog
import com.mifos.passcode.auth.passcode.components.SystemAuthenticatorButton
import com.mifos.passcode.getPlatform
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource


@Composable
fun DeviceAuthScreen(
    platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption? = null,
    onDeviceAuthSuccess: () -> Unit = {},
) {

    val scope = rememberCoroutineScope()

    val platformAuthenticationProvider = PlatformAuthenticationProvider(
        LocalPlatformAuthenticator.current,
        scope
    )

    val authenticationResult by platformAuthenticationProvider.authenticationResult.collectAsStateWithLifecycle()

    val authenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsStateWithLifecycle()

    var showAuthPrompt by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(authenticationResult){
        if(authenticationResult == AuthenticationResult.Success()){
            onDeviceAuthSuccess()
        }
    }

    val appName = stringResource(Res.string.app_name)

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
                    println("System Authentication requested.")
                    showAuthPrompt = true
                },
                platformAuthOptions = platformAvailableAuthenticationOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = authenticatorStatus,
                platform = getPlatform()
            )

            if(!authenticatorStatus.userCredentialSet && showAuthPrompt){
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
    }

}

