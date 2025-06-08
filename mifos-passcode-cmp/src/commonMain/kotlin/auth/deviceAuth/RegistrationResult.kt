package auth.deviceAuth

sealed interface RegistrationResult {
    data class Success(val registrationData: String) : RegistrationResult
    data class Error(val message: String) : RegistrationResult
    data object PlatformAuthenticatorNotSet : RegistrationResult
}