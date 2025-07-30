package mifos.authenticator.biometrics.platformAuthenticator

sealed interface RegistrationResult {
    data class Success(val message: String) : RegistrationResult
    data class Error(val message: String) : RegistrationResult
    data object PlatformAuthenticatorNotSet : RegistrationResult
    data object PlatformAuthenticatorNotAvailable : RegistrationResult
}