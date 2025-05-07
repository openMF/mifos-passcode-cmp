package com.mifos.passcode.ui.screen

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.passcode.LocalAndroidActivity
import com.mifos.passcode.LocalPlatformAuthenticator
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.auth.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.getPlatform
import com.mifos.passcode.ui.component.MifosIcon
import com.mifos.passcode.ui.components.SystemAuthSetupConfirmDialog
import com.mifos.passcode.ui.components.SystemAuthenticatorButton
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.stringResource
import kotlin.native.concurrent.ThreadLocal


@Composable
fun DeviceAuthScreen(
    authOption: AuthOption? = null,
    onDeviceAuthSuccess: () -> Unit = {},
) {

    val deviceAuthenticatorViewModel = DeviceAuthenticatorViewModel(LocalPlatformAuthenticator.current)

    val authenticationResult = deviceAuthenticatorViewModel.authenticationResult.collectAsStateWithLifecycle()

    val authenticatorStatus = deviceAuthenticatorViewModel.authenticatorStatus.collectAsStateWithLifecycle()

    var showAuthPrompt by rememberSaveable() {
        mutableStateOf(false)
    }


    if(authenticationResult.value == AuthenticationResult.Success() && showAuthPrompt){
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
                    showAuthPrompt = true
                },
                platformAuthOptions = authOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = authenticatorStatus.value,
                platform = getPlatform()
            )

            if(!authenticatorStatus.value.biometricsSet && showAuthPrompt){
                deviceAuthenticatorViewModel.getDeviceAuthenticatorStatus()
                if(!authenticatorStatus.value.biometricsSet){
                    SystemAuthSetupConfirmDialog(
                        cancelSetup = {
                            showAuthPrompt = false
                            deviceAuthenticatorViewModel.getDeviceAuthenticatorStatus()
                        },
                        setSystemAuthentication = {
                            deviceAuthenticatorViewModel.setupDeviceAuthenticator()
                            showAuthPrompt = false
                            deviceAuthenticatorViewModel.getDeviceAuthenticatorStatus()
                        }
                    )
                }
            }

        }
    }

}


