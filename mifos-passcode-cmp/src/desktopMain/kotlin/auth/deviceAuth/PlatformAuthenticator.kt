package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.windows.WindowsHelloAuthenticatorNativeSupportImpl
import com.mifos.passcode.auth.deviceAuth.windows.utils.decodeWindowsAuthenticatorFromJson
import com.mifos.passcode.auth.deviceAuth.windows.utils.encodeWindowsAuthenticatorToJsonString
import com.mifos.passcode.auth.deviceAuth.windows.utils.isWindowsTenOrEleven
import com.mifos.passcode.mock_server.WindowsAuthenticationResponse
import com.mifos.passcode.mock_server.models.WindowsAuthenticatorResponse
import com.mifos.passcode.mock_server.models.WindowsHelloAuthenticator
import com.mifos.passcode.mock_server.models.WindowsRegistrationResponse
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

    actual fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus {
        if(isWindowsTenOrHigh){
            println("Windows Ten or Eleven")
            return PlatformAuthenticatorStatus.DesktopAuthenticatorStatus.WindowsAuthenticatorStatus(windowsHelloAuthenticator.checkIfWindowsHelloSupportedOrNot())
        }
        println("Unsupported Platform")
        return PlatformAuthenticatorStatus.UnsupportedPlatform()
    }


    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(): Pair<AuthenticationResult,String> {
        if(isWindowsTenOrHigh){
            val windowsAuthResponse = windowsHelloAuthenticator.invokeUserRegistration()

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Registration.Error){
                return Pair(AuthenticationResult.Error("Error while registering user"), "")
            }
            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Registration.Success).response.windowsAuthenticationResponse
            println("Response $response")
            return if(response == WindowsAuthenticationResponse.SUCCESS){
                Pair(returnAuthenticatorResult(
                    response), encodeWindowsAuthenticatorToJsonString(windowsAuthResponse.response)
                )
            }else Pair(returnAuthenticatorResult(response), "")
        }
        return Pair(AuthenticationResult.Error("Coming Soon"), "")
    }


    @OptIn(ExperimentalStdlibApi::class)
    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {

        if(isWindowsTenOrHigh){
            val windowsRegistrationResponse: WindowsRegistrationResponse? = savedRegistrationOutput?.let {
                decodeWindowsAuthenticatorFromJson(savedRegistrationOutput)
            } ?: return AuthenticationResult.Error("Invalid registration data")

            val windowsAuthResponse: WindowsAuthenticatorResponse.Verification? = windowsRegistrationResponse?.let { response ->
                windowsHelloAuthenticator.invokeUserVerification(response)
            } ?: return AuthenticationResult.Error("Unknown error while authenticating user")

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Verification.Error) {
                return AuthenticationResult.Error("Error while registering user")
            }

            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Verification.Success).response
            println("Response $response")
            return returnAuthenticatorResult(response)
        }

        return AuthenticationResult.Error("Coming Soon")
    }
}

fun returnAuthenticatorResult(windowsAuthenticatorResponse: WindowsAuthenticationResponse): AuthenticationResult {
    return when(windowsAuthenticatorResponse){
        WindowsAuthenticationResponse.SUCCESS -> AuthenticationResult.Success()
        WindowsAuthenticationResponse.UNSUCCESSFUL -> AuthenticationResult.Failed(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.E_FAILURE -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.ABORTED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.USER_CANCELED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.REGISTER_AGAIN -> AuthenticationResult.RegisterAgain
        WindowsAuthenticationResponse.UNKNOWN_ERROR -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.INVALID_PARAMETER -> AuthenticationResult.Error("${windowsAuthenticatorResponse.name}: Invalid parameters user for authentication")
    }
}