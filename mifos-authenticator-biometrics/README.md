# Mifos Authenticator Biometrics

This module provides a unified and multiplatform way to handle device-based authentication. It uses a `PlatformAuthenticator` to interact with platform-specific mechanisms (like Windows Hello or Android BiometricPrompt) and wraps it in a thread-safe `PlatformAuthenticationProvider` for easy and safe use in your application.

## Installation

Add the `io.github.openmf:mifos-authenticator-passcode` dependency to your `build.gradle.kts` file:

---

## Quick Start

### 1. Create the Authenticator and Provider

On **Android**, you must pass a `FragmentActivity` or `AppCompatActivity`. On other platforms, this is not required.

```kotlin
// On Android
val authenticator = PlatformAuthenticator(this) // 'this' is your FragmentActivity
val authProvider = PlatformAuthenticationProvider(this)

// On other platforms
val authenticator = PlatformAuthenticator()
val authProvider = PlatformAuthenticationProvider()
```

### 2. Check Authentication Status

```kotlin
val status = authProvider.deviceAuthenticatorStatus()

if (status.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)) {
    // Biometrics are available and configured
} else if (status.contains(PlatformAuthenticatorStatus.NOT_SETUP)) {
    // User needs to set up authentication
    authProvider.setupPlatformAuthenticator()
}
```

### 3. Register User (Required for Windows)

```kotlin
viewModelScope.launch(Dispatchers.Main) { // Must be on Main thread for Android
    val result = authProvider.registerUser(
        userName = "mifosUser123",
        emailId = "user@mifos.org",
        displayName = "Mifos User"
    )

    when (result) {
        is RegistrationResult.Success -> {
            // Save the registration data for later authentication
            val registrationData = result.message
            saveRegistrationData(registrationData)
        }
        is RegistrationResult.Error -> {
            showError(result.message)
        }
        RegistrationResult.PlatformAuthenticatorNotSet -> {
            promptUserToSetup()
        }
        RegistrationResult.PlatformAuthenticatorNotAvailable -> {
            showNotAvailableMessage()
        }
    }
}
```

### 4. Authenticate User

```kotlin
viewModelScope.launch(Dispatchers.Main) { // Must be on Main thread for Android
    val savedData = getRegistrationData() // Retrieve saved registration data
    val result = authProvider.onAuthenticatorClick(
        appName = "My App",
        savedRegistrationData = savedData
    )

    when (result) {
        AuthenticationResult.Success -> {
            // Authentication successful, proceed with app
        }
        is AuthenticationResult.Error -> {
            showError(result.message)
        }
        AuthenticationResult.UserNotRegistered -> {
            // User needs to register again
            logoutAndRedirectToRegistration()
        }
    }
}
```

---

## API Reference

### `PlatformAuthenticationProvider` (Main Interface)

This is the primary class you should interact with. It provides a thread-safe facade over the platform authenticator.

```kotlin
class PlatformAuthenticationProvider(activity: Any? = null) {

    // Observable status of the device authenticator
    val authenticatorStatus: StateFlow<Set<PlatformAuthenticatorStatus>>

    // Updates the authenticator status (call before registration/authentication)
    fun updateAuthenticatorStatus()

    // Registers a user and creates a platform-specific credential
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = ""
    ): RegistrationResult

    // Authenticates the user against their registered credential
    suspend fun onAuthenticatorClick(
        appName: String = "",
        savedRegistrationData: String? = null
    ): AuthenticationResult

    // Prompts the user to set up platform authentication
    fun setupPlatformAuthenticator()
}
```

### `PlatformAuthenticator` (Underlying Engine)

This `expect class` contains the core platform-specific logic. It's managed by `PlatformAuthenticationProvider`.

```kotlin
expect class PlatformAuthenticator private constructor() {
    constructor(activity: Any? = null)

    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>
    fun setDeviceAuthOption()

    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = ""
    ): RegistrationResult

    suspend fun authenticate(
        title: String = "",
        savedRegistrationOutput: String?
    ): AuthenticationResult
}
```

### `PlatformAuthenticatorStatus` (Enum)

The `getDeviceAuthenticatorStatus()` function returns a set of the following values:

- `NOT_AVAILABLE` – Platform authenticator is not supported on the device
- `NOT_SETUP` – Authenticator is available but not configured
- `DEVICE_CREDENTIAL_SET` – Device credential (PIN, password, etc.) is configured
- `BIOMETRICS_NOT_SET` – Biometrics are supported but not enrolled
- `BIOMETRICS_NOT_AVAILABLE` – Biometrics are not available on the device
- `BIOMETRICS_UNAVAILABLE` – Biometrics are temporarily unavailable (e.g., too many failed attempts)
- `BIOMETRICS_SET` – Biometrics are available and configured

### `RegistrationResult` (Sealed Interface)

Returned by `registerUser()`:

```kotlin
sealed interface RegistrationResult {
    data class Success(val message: String) : RegistrationResult
    data class Error(val message: String) : RegistrationResult
    data object PlatformAuthenticatorNotSet : RegistrationResult
    data object PlatformAuthenticatorNotAvailable : RegistrationResult
}
```

**Important:** The `Success.message` contains registration data that **must be saved** and passed to `onAuthenticatorClick()` for authentication.

### `AuthenticationResult` (Sealed Interface)

Returned by `onAuthenticatorClick()`:

```kotlin
sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Error(val message: String) : AuthenticationResult
    data object UserNotRegistered : AuthenticationResult
}
```

---

## Platform-Specific Implementations

### Android

Uses `androidx.biometric.BiometricPrompt` API:
- Supports fingerprint, face recognition, and device credentials
- Requires `FragmentActivity` or `AppCompatActivity` context
- **Must be called from the Main thread**
- Supports both strong and weak biometric authentication

### iOS

Uses `LocalAuthentication` framework:
- Supports Touch ID and Face ID
- Falls back to device passcode
- No special context required

### Windows (Desktop)

Uses Windows Hello API via JNA:
- Supports Windows Hello biometrics
- Uses WebAuthn for passkey creation
- Requires user registration before authentication
- Registration data must be saved and provided during authentication

### Web (JS/Wasm)

Uses Web Authentication API (WebAuthn):
- Browser-dependent authentication methods
- Supports platform authenticators (device biometrics)
- Requires HTTPS in production
- Registration creates a credential stored in the browser

---

## Important Notes

### Thread Requirement (Android)

The Android `BiometricPrompt` API requires invocation from the **main thread**. Always call `registerUser()` and `onAuthenticatorClick()` from the Main dispatcher:

```kotlin
// Correct - using Main dispatcher
viewModelScope.launch(Dispatchers.Main) {
    authProvider.registerUser(...)
    authProvider.onAuthenticatorClick(...)
}

// Using viewModelScope is safe by default (runs on Main)
viewModelScope.launch {
    authProvider.registerUser(...)
}
```

### Registration Data Storage

For platforms like Windows and Web, registration creates a credential that must be stored:

```kotlin
when (val result = authProvider.registerUser(...)) {
    is RegistrationResult.Success -> {
        // CRITICAL: Save this data securely
        val registrationData = result.message
        preferenceStore.save(REGISTRATION_KEY, registrationData)
    }
    // ...
}
```

### Lifecycle Management

Update the authenticator status when your app resumes or when platform settings might have changed:

```kotlin
override fun onResume() {
    super.onResume()
    authProvider.updateAuthenticatorStatus()
}
```

---

## ViewModel Integration Examples

### Registration ViewModel

```kotlin
class RegistrationViewModel(
    private val authProvider: PlatformAuthenticationProvider,
    private val preferenceStore: PreferenceDataStore
) : ViewModel() {

    private val _registrationResult = MutableStateFlow<RegistrationResult?>(null)
    val registrationResult = _registrationResult.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(authProvider.deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    fun updateAuthenticatorStatus() {
        authProvider.updateAuthenticatorStatus()
        _authenticatorStatus.value = authProvider.authenticatorStatus.value
    }

    fun registerUser(
        userID: String = "",
        userEmail: String = "",
        displayName: String = ""
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val result = authProvider.registerUser(userID, userEmail, displayName)
            _registrationResult.value = result

            // Save registration data if successful
            if (result is RegistrationResult.Success) {
                preferenceStore.saveRegistrationData(result.message)
            }
        }
    }

    fun setupAuthenticator() {
        authProvider.setupPlatformAuthenticator()
    }
}
```

### Authentication ViewModel

```kotlin
class AuthenticationViewModel(
    private val authProvider: PlatformAuthenticationProvider,
    private val preferenceStore: PreferenceDataStore
) : ViewModel() {

    private val _authResult = MutableStateFlow<AuthenticationResult?>(null)
    val authResult = _authResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun authenticateUser(appName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = true

            val savedData = preferenceStore.getRegistrationData()
            val result = authProvider.onAuthenticatorClick(appName, savedData)

            _authResult.value = result
            _isLoading.value = false

            // Handle user not registered
            if (result is AuthenticationResult.UserNotRegistered) {
                clearUserData()
            }
        }
    }

    private fun clearUserData() {
        preferenceStore.clearRegistrationData()
    }
}
```

---

## Common Usage Patterns

### Check Status Before Registration

```kotlin
fun attemptRegistration() {
    val status = authProvider.deviceAuthenticatorStatus()

    when {
        status.contains(PlatformAuthenticatorStatus.NOT_AVAILABLE) -> {
            showMessage("Authentication not available on this device")
        }
        status.contains(PlatformAuthenticatorStatus.NOT_SETUP) -> {
            showMessage("Please set up device authentication first")
            authProvider.setupPlatformAuthenticator()
        }
        else -> {
            registerUser()
        }
    }
}
```

### Observe Status Changes

```kotlin
class AuthSetupScreen(
    private val authProvider: PlatformAuthenticationProvider
) {
    init {
        viewModelScope.launch {
            authProvider.authenticatorStatus.collect { statusSet ->
                val canAuthenticate = statusSet.contains(
                    PlatformAuthenticatorStatus.BIOMETRICS_SET
                ) || statusSet.contains(
                    PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET
                )

                updateUI(canAuthenticate)
            }
        }
    }
}
```

---

## Troubleshooting

### Android: BiometricPrompt Not Showing

- Ensure you're passing a `FragmentActivity`, not a regular `Activity`
- Verify the call is made from the Main thread
- Check that biometrics or device credentials are set up on the device

### Windows: Registration Fails

- Ensure Windows Hello is enabled in system settings
- User must have a PIN set up before using Windows Hello
- Check that the application has necessary permissions

### Web: Authentication Not Working

- Verify the site is served over HTTPS (required for WebAuthn)
- Check browser compatibility (modern browsers only)
- Ensure the user has allowed the credential creation

### iOS: Face ID/Touch ID Not Available

- Check that biometrics are enrolled in iOS Settings
- Verify app has `NSFaceIDUsageDescription` in Info.plist
- Ensure device supports Face ID or Touch ID

