package com.mifos.passcode.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.auth.deviceAuth.AuthenticatorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeviceAuthenticatorViewModel(
    val platformAuthenticator: PlatformAuthenticator
): ViewModel(){

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()
    private val _authenticatorStatus = MutableStateFlow(AuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.stateIn(
        viewModelScope,
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
        viewModelScope.launch {
            if(_authenticatorStatus.value.biometricsSet || _authenticatorStatus.value.userCredentialSet){
                showDeviceAuthenticatorPrompt(title)
            }
        }
    }

    fun setupDeviceAuthenticator(){
        platformAuthenticator.setDeviceAuthOption()
    }
}
