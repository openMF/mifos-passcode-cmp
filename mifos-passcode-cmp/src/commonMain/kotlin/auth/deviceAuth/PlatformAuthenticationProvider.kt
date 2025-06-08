package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.Composable
import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages platform-specific user authentication (e.g., Biometrics, Windows Hello).
 *
 * This class handles user registration and authentication flows, providing reactive state
 * for the current availability and status of platform authenticators. It is designed to be
 * lifecycle-aware via the [updateAuthenticatorStatus] method.
 *
 * @param activity provided current activity in Android. It can be null for other platforms.
 */

final class PlatformAuthenticationProvider(val activity: Any? = null){

    private val authenticator = PlatformAuthenticator(activity)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())
    val authenticatorStatus: StateFlow<PlatformAuthenticatorStatus> = _authenticatorStatus.asStateFlow()

    // Check the support for platform authenticator according to the platform
    private fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()

    fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    suspend fun registerUser(): RegistrationResult {
        updateAuthenticatorStatus()
        if (_isLoading.value) {
            println("Registration already in progress, ignoring new request.")
            return RegistrationResult.Error("Registration already in progress, ignoring new request.")
        }
        if(!isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)) {
            return RegistrationResult.PlatformAuthenticatorNotSet
        }
        try {
            _isLoading.value = true
            return authenticator.registerUser()
        } catch (e: Exception) {
            return RegistrationResult.Error("Registration failed: ${e.message}")
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
        if(!isPlatformAuthenticatorSupportAvailable(authenticatorStatus.value)) {
            return AuthenticationResult.UserNotRegistered
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
}



