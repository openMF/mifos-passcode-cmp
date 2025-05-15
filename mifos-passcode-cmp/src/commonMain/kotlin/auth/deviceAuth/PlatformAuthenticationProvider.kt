package com.mifos.passcode.auth.deviceAuth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlatformAuthenticationProvider(
    private val platformAuthenticator: PlatformAuthenticator,
    private val scope: CoroutineScope
){
    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()
    private val _authenticatorStatus = MutableStateFlow(AuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.stateIn(
        scope,
        started = SharingStarted.Eagerly,
        initialValue = deviceAuthenticatorStatus()
    )

    init {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }
    private fun deviceAuthenticatorStatus() = platformAuthenticator.getDeviceAuthenticatorStatus()

    fun getDeviceAuthenticatorStatus(){
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    suspend fun showDeviceAuthenticatorPrompt(title: String) {
        _authenticationResult.value = platformAuthenticator.authenticate(title)
    }

    fun onAuthenticatorClick(title: String= "") {
        scope.launch {
            if(_authenticatorStatus.value.biometricsSet || _authenticatorStatus.value.userCredentialSet){
                showDeviceAuthenticatorPrompt(title)
            }
        }
    }

    fun setupDeviceAuthenticator(){
        platformAuthenticator.setDeviceAuthOption()
    }
}
