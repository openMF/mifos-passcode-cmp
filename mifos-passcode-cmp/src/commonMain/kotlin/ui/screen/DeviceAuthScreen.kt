package com.mifos.passcode.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.getPlatform
import com.mifos.passcode.ui.component.MifosIcon
import com.mifos.passcode.ui.components.SystemAuthSetupConfirmDialog
import com.mifos.passcode.ui.components.SystemAuthenticatorButton
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource


@Composable
fun DeviceAuthScreen(
    authOption: AuthOption? = null,
    onDeviceAuthSuccess: () -> Unit = {},
    deviceAuthenticatorViewModel: DeviceAuthenticatorViewModel,
) {


    val authenticationResult =
        deviceAuthenticatorViewModel.authenticationResult.collectAsStateWithLifecycle()


    val authenticatorStatus =
        deviceAuthenticatorViewModel.authenticatorStatus.collectAsStateWithLifecycle()


    val showAuthPrompt = rememberSaveable() {
        mutableStateOf(false)
    }

    val showSetBiometricDialog = rememberSaveable(){ mutableStateOf(false) }


    if(authenticationResult.value == AuthenticationResult.Success() && showAuthPrompt.value){
        onDeviceAuthSuccess()
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
                    deviceAuthenticatorViewModel.onAuthenticatorClick(appName)
                    println("System Authentication requested.")
                    showAuthPrompt.value = true
                },
                platformAuthOptions = authOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = authenticatorStatus.value,
                platform = getPlatform()
            )

            if(showSetBiometricDialog.value && showAuthPrompt.value){
                SystemAuthSetupConfirmDialog(
                    cancelSetup = {
                        showSetBiometricDialog.value = false
                        showAuthPrompt.value = false
                    },
                    setSystemAuthentication = {
                        deviceAuthenticatorViewModel.setupDeviceAuthenticator()
                        showSetBiometricDialog.value = false
                        showAuthPrompt.value = false
                    }
                )
            }

        }
    }

}


