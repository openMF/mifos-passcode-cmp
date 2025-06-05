package com.mifos.passcode.auth.deviceAuth

data class AuthenticatorStatus(
    var biometricsSet: Boolean = false,
    var message: String = "",
    var biometricsNotPossible: Boolean = true,
    var userCredentialSet: Boolean = false
)

sealed class PlatformAuthenticatorStatus {

    class UnsupportedPlatform : PlatformAuthenticatorStatus()

    data class MobileAuthenticatorStatus(
        var biometricsSet: Boolean = false,
        var message: String = "",
        var biometricsNotPossible: Boolean = true,
        var userCredentialSet: Boolean = false
    ) : PlatformAuthenticatorStatus()

    sealed class DesktopAuthenticatorStatus: PlatformAuthenticatorStatus() {
        data class WindowsAuthenticatorStatus(
            var windowsHelloSupported: Boolean = false,
        ): DesktopAuthenticatorStatus()
    }

    data class WebAuthenticatorStatus(
        var browserSupported: Boolean = false,
    ): PlatformAuthenticatorStatus() {
//        class JsAuthenticatorStatus(
//            var browserSupported: Boolean = false,
//        ): WebAuthenticatorStatus()
//        class WasmJsAuthenticatorStatus(
//            var browserSupported: Boolean = false,
//        ): WebAuthenticatorStatus()
    }
}