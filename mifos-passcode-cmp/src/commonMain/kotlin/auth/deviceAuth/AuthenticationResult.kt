package auth.deviceAuth

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Error(val message: String) : AuthenticationResult
    data object UserNotRegistered : AuthenticationResult
}