package com.mifos.passcode.auth.deviceAuth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


final class PlatformAuthenticationProvider(private val authenticator: PlatformAuthenticator, ){

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())
    val authenticatorStatus: StateFlow<PlatformAuthenticatorStatus> = _authenticatorStatus.asStateFlow()


    // Check the support for platform authenticator according to the platform
    fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()

    fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    suspend fun registerUser(): Pair<AuthenticationResult, String> {
        updateAuthenticatorStatus()
        if (_isLoading.value) {
            println("Registration already in progress, ignoring new request.")
            return Pair(AuthenticationResult.Error("User already exists"),"")
        }
        if(!isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)) {
            return Pair(AuthenticationResult.PlatformAuthenticatorNotSet, "")
        }
        try {
            _isLoading.value = true
            return authenticator.registerUser()
        } catch (e: Exception) {
            return Pair(AuthenticationResult.Error("Registration failed: ${e.message}"),"")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun onAuthenticatorClick(appName: String= "", savedRegistrationData: String?=null): AuthenticationResult {
        updateAuthenticatorStatus()
        if (_isLoading.value) {
            println("Authentication already in progress, ignoring new request.")
            return AuthenticationResult.Error("User already exists")
        }
        if(!isPlatformAuthenticatorSupportAvailable(deviceAuthenticatorStatus())) {
            return AuthenticationResult.RegisterAgain
        }
        try {
            println("Saved data: $savedRegistrationData")
            _isLoading.value = true
            return authenticator.authenticate(appName, savedRegistrationData)
        } catch (e: Exception) {
            return AuthenticationResult.Error("Authentication failed: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    fun setupDeviceAuthenticator(){
        authenticator.setDeviceAuthOption()
    }
}


fun isPlatformAuthenticatorSupportAvailable(platformAuthenticatorStatus: PlatformAuthenticatorStatus): Boolean{
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
