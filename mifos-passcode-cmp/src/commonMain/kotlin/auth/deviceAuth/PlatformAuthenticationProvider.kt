package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.SavedStateHandle
import com.mifos.passcode.LibraryLocalAndroidActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn



final class PlatformAuthenticationProvider(
    private val savedRegistrationData: String?,
    private val authenticator: PlatformAuthenticator,
    scope: CoroutineScope,
){
    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val authenticatorStatus: StateFlow<PlatformAuthenticatorStatus> = flow {
        while (true) {
            emit(deviceAuthenticatorStatus())
            delay(5000)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = deviceAuthenticatorStatus()
    )


    // Check the support for platform authenticator according to the platform
    private fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()


    suspend fun registerUser(){
        try {
            _isLoading.value = true
            if(isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)){
                _authenticationResult.value = authenticator.registerUser()
            }
        } catch (e: Exception) {
            _authenticationResult.value = AuthenticationResult.Error("Registration failed: ${e.message}")
        } finally {
            _isLoading.value = false
        }

    }

    suspend fun onAuthenticatorClick(appName: String= "") {
        try {
            println("Saved data: $savedRegistrationData")
            if(isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)) {
                _authenticationResult.value = authenticator.authenticate(appName, savedRegistrationData)
            }
        } catch (e: Exception) {
            _authenticationResult.value = AuthenticationResult.Error("Registration failed: ${e.message}")
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
