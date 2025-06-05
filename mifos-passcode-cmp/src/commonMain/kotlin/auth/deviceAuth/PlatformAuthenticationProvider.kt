package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Composable
fun rememberPlatformAuthenticationProvider(
    platformAuthenticator: PlatformAuthenticator,
    registrationData: String
): PlatformAuthenticationProvider {

    val scope = rememberCoroutineScope()

    return remember(
        key1 = registrationData.isEmpty()
    ){
        PlatformAuthenticationProvider(
            platformAuthenticator = platformAuthenticator,
            registrationData = registrationData,
            scope = scope,
        )
    }

}

final class PlatformAuthenticationProvider(
    private val platformAuthenticator: PlatformAuthenticator,
    private val registrationData: String,
    private val scope: CoroutineScope
){
    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _authenticatorStatus: MutableStateFlow<PlatformAuthenticatorStatus?> = MutableStateFlow(null)
    val authenticatorStatus = _authenticatorStatus.stateIn(
        scope,
        started = SharingStarted.Eagerly,
        initialValue = deviceAuthenticatorStatus()
    )

    init {
        _authenticatorStatus.value = deviceAuthenticatorStatus()

        registerUser()
    }

    // Check the support for platform authenticator according to the platform
    private fun deviceAuthenticatorStatus() = platformAuthenticator.getDeviceAuthenticatorStatus()

    private suspend fun showDeviceAuthenticatorPrompt(title: String){
        _authenticationResult.value = platformAuthenticator.authenticate(title, registrationData)
    }

    fun getDeviceAuthenticatorStatus(){
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    fun registerUser(){
        scope.launch {
            _authenticatorStatus.value?.let {
                if(platformAuthenticatorSupportAvailable(it)){
                    _authenticationResult.value = platformAuthenticator.registerUser()
                }
            }
        }
    }



    fun onAuthenticatorClick(appName: String= "") {
        scope.launch {
            _authenticatorStatus.value?.let {
                if(platformAuthenticatorSupportAvailable(it)){
                    showDeviceAuthenticatorPrompt(appName)
                }
            }
        }
    }

    fun setupDeviceAuthenticator(){
        platformAuthenticator.setDeviceAuthOption()
    }
}


fun platformAuthenticatorSupportAvailable(platformAuthenticatorStatus: PlatformAuthenticatorStatus): Boolean{
    return when(platformAuthenticatorStatus){
        is PlatformAuthenticatorStatus.MobileAuthenticatorStatus -> {
            platformAuthenticatorStatus.biometricsSet || platformAuthenticatorStatus.userCredentialSet
        }
        is PlatformAuthenticatorStatus.DesktopAuthenticatorStatus.WindowsAuthenticatorStatus -> {
            platformAuthenticatorStatus.windowsHelloSupported
        }
        is PlatformAuthenticatorStatus.WebAuthenticatorStatus -> {
            platformAuthenticatorStatus.browserSupported
        }
        is PlatformAuthenticatorStatus.UnsupportedPlatform -> {
            false
        }
    }
}
