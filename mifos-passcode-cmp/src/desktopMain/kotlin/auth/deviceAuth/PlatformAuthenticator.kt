package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
import auth.deviceAuth.windows.WindowsHelloAuthenticatorNativeSupportImpl
import com.mifos.passcode.auth.deviceAuth.windows.utils.decodeWindowsAuthenticatorFromJson
import com.mifos.passcode.auth.deviceAuth.windows.utils.encodeWindowsAuthenticatorToJsonString
import com.mifos.passcode.auth.deviceAuth.windows.utils.isWindowsTenOrEleven
import com.mifos.passcode.mockServer.WindowsAuthenticationResponse
import com.mifos.passcode.auth.deviceAuth.windows.WindowsAuthenticatorResponse
import com.mifos.passcode.auth.deviceAuth.windows.WindowsHelloAuthenticator
import com.mifos.passcode.auth.deviceAuth.windows.WindowsRegistrationResponse
import com.sun.jna.Platform


actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()

    private val isWindowsTenOrHigh = if(Platform.isWindows()) isWindowsTenOrEleven()  else false

    private val windowsHelloAuthenticatorNativeSupport by lazy {
        WindowsHelloAuthenticatorNativeSupportImpl()
    }

    private val windowsHelloAuthenticator by lazy {
        WindowsHelloAuthenticator(
            windowsHelloAuthenticatorNativeSupport
        )
    }

    actual fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus> {
        if(isWindowsTenOrHigh){
            println("Windows Ten or Eleven")
            val isWindowsHelloAvailable = windowsHelloAuthenticator.checkIfWindowsHelloSupportedOrNot()
            if(isWindowsHelloAvailable) return setOf(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
            return setOf(PlatformAuthenticatorStatus.NOT_SETUP)
        }
        println("Unsupported Platform")
        return setOf(PlatformAuthenticatorStatus.NOT_AVAILABLE)
    }


    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
    ): RegistrationResult {
        if(isWindowsTenOrHigh){
            val windowsAuthResponse = windowsHelloAuthenticator.invokeUserRegistration(
                userName,
                emailId,
                displayName,
            )

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Registration.Error){
                return RegistrationResult.Error("Error while registering user")
            }
            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Registration.Success).response.windowsAuthenticationResponse
            println("Response $response")
            return if(response == WindowsAuthenticationResponse.SUCCESS){
                RegistrationResult.Success(encodeWindowsAuthenticatorToJsonString(windowsAuthResponse.response))
            } else returnRegistrationResult(windowsAuthResponse.response)
        }
        return RegistrationResult.PlatformAuthenticatorNotAvailable
    }


    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {

        if(isWindowsTenOrHigh){
            val windowsRegistrationResponse: WindowsRegistrationResponse = savedRegistrationOutput?.let {
                decodeWindowsAuthenticatorFromJson(savedRegistrationOutput)
            } ?: return AuthenticationResult.Error("Invalid registration data")

            val windowsAuthResponse: WindowsAuthenticatorResponse.Verification = windowsHelloAuthenticator.invokeUserVerification(windowsRegistrationResponse)

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Verification.Error) {
                return AuthenticationResult.Error("Error while registering user")
            }

            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Verification.Success).response
            println("Response $response")
            return returnAuthenticatorResult(response)
        }

        return AuthenticationResult.UserNotRegistered
    }
}

fun returnAuthenticatorResult(windowsAuthenticatorResponse: WindowsAuthenticationResponse): AuthenticationResult {
    return when(windowsAuthenticatorResponse){
        WindowsAuthenticationResponse.SUCCESS -> AuthenticationResult.Success
        WindowsAuthenticationResponse.UNSUCCESSFUL -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.E_FAILURE -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.ABORTED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.USER_CANCELED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.REGISTER_AGAIN -> AuthenticationResult.UserNotRegistered
        WindowsAuthenticationResponse.UNKNOWN_ERROR -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.INVALID_PARAMETER -> AuthenticationResult.Error("${windowsAuthenticatorResponse.name}: Invalid arguments used for authentication")
    }
}

fun returnRegistrationResult(windowsRegistrationResponse: WindowsRegistrationResponse): RegistrationResult{
    return when(windowsRegistrationResponse.windowsAuthenticationResponse){
        WindowsAuthenticationResponse.SUCCESS -> RegistrationResult.Success(
            encodeWindowsAuthenticatorToJsonString(windowsRegistrationResponse)
        )
        WindowsAuthenticationResponse.UNSUCCESSFUL -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.E_FAILURE -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.ABORTED -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.USER_CANCELED -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.REGISTER_AGAIN -> RegistrationResult.PlatformAuthenticatorNotSet
        WindowsAuthenticationResponse.UNKNOWN_ERROR -> RegistrationResult.Error(windowsRegistrationResponse.windowsAuthenticationResponse.name)
        WindowsAuthenticationResponse.INVALID_PARAMETER -> RegistrationResult.Error("${windowsRegistrationResponse.windowsAuthenticationResponse.name}: Invalid arguments used for registration.")
    }
}

