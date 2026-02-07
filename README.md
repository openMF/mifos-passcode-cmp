<div align="center">
<img src="https://user-images.githubusercontent.com/37406965/51083189-d5dc3a80-173b-11e9-8ca0-28015e0893ac.png" alt="Android Client" />
	
## Mifos-Passcode-CMP

Mifos-Passcode-CMP is a secure and flexible App Lock library built using Kotlin Multiplatform and Jetpack Compose Multiplatform (CMP). It enables developers to easily integrate passcode-based authentication along with biometric authentication (such as fingerprint or face recognition) into cross-platform applications using a shared codebase.

Designed with modularity and security in mind, this library is a foundational part of the Mifos mobile ecosystem and is suitable for any Kotlin Multiplatform project where secure access control is required.


![Kotlin](https://img.shields.io/badge/Kotlin-7f52ff?style=flat-square&logo=kotlin&logoColor=white)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-4c8d3f?style=flat-square&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Jetpack%20Compose%20Multiplatform-000000?style=flat-square&logo=android&logoColor=white)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-macos](https://img.shields.io/badge/platform-macos-CDCDCD)
![badge-windows](https://img.shields.io/badge/platform-windows-blue)
![badge-linux](https://img.shields.io/badge/platform-linux-orange)
![badge-web](http://img.shields.io/badge/platform-web-FDD835.svg?style=flat)

[![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)
</div>

---

# ✅ Supported Platforms

| Platform   | Passcode	      | Platform Authenticator |
|------------|----------------|------------------------|
| Android    | ✅ Supported   | ✅ Supported 
| iOS        | ✅ Supported   | ✅ Supported
| macOS      | ✅ Supported   ||
| Windows 10+| ✅ Supported   | ✅ Supported 
| Linux      | ✅ Supported   ||
| Web        | ✅ Supported   ||


---

## Project Structure

The project is organized into several modules to ensure a clear separation of concerns and to facilitate multiplatform development:

- **`build-logic`**: Contains Gradle convention plugins used to standardize build configurations across all modules (linting, static analysis, multiplatform setup).
- **`mifos-authenticator-passcode`**: A Compose Multiplatform library providing the logic and UI components for passcode authentication.
- **`mifos-authenticator-biometrics`**: A library providing platform-specific implementations for device authentication (Biometrics, Windows Hello, WebAuthn).
- **`cmp-sample-shared`**: Contains the shared business logic, navigation, and UI for the sample applications.
- **`cmp-sample-android`**: The Android entry point for the sample application.
- **`cmp-sample-ios`**: The iOS entry point for the sample application.
- **`cmp-sample-desktop`**: The Desktop (JVM) entry point for the sample application.
- **`cmp-sample-web`**: The Web (Wasm & JS) entry point for the sample application.

### `mifos-authenticator-biometrics`
- **`commonMain/`**: Platform-agnostic interface and provider for device authentication.
- **`androidMain/`**: Implementation using Android `BiometricPrompt`.
- **`iosMain/`**: Implementation using iOS `LocalAuthentication`.
- **`desktopMain/`**: Implementation using JNA for Windows Hello (on Windows) and placeholders for other desktop platforms.
- **`jsMain/` / `wasmJsMain/`**: Implementation using the Web Authentication API (WebAuthn).

### `mifos-authenticator-passcode`
- **`commonMain/`**: Contains the `PasscodeManager`, `PasscodeStorageAdapter`, and the `PasscodeScreen` UI built with Compose Multiplatform.

### `cmp-sample-shared`
- **`commonMain/`**: Defines the shared UI, themes, and navigation logic. It integrates both the passcode and biometric modules to demonstrate a complete authentication flow.

---

## Platform Authenticator Usage

This module provides a unified and multiplatform way to handle device-based authentication. It uses a `PlatformAuthenticator` to interact with platform-specific mechanisms (like Windows Hello or Android BiometricPrompt) and wraps it in a thread-safe `PlatformAuthenticationProvider` for easy and safe use in your application.

---

## Getting Started

To use the platform authenticator, first create an instance of `PlatformAuthenticator`, and then pass it to PlatformAuthenticationProvider.

On `Android`, you must pass a `FragmentActivity`  or an  `AppCompatActivity`. On other platforms, this is not required. <br>

```kotlin
// 1. Create the platform-specific authenticator instance
// On Android
val authenticator = PlatformAuthenticator(this) 
// On other platforms
val authenticator = PlatformAuthenticator()

// 2. Create the provider instance to interact with
val authProvider = PlatformAuthenticationProvider(authenticator)
```
---

## API Overview

### `PlatformAuthenticationProvider` (Recommended)
This is the main class you should interact with. It acts as a thread-safe facade that simplifies using the `PlatformAuthenticator`.

```kotlin
final class PlatformAuthenticationProvider(private val authenticator: PlatformAuthenticator) {

    // Checks the current status of the device authenticator.
    fun deviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>

    // Prompts the user to set up a platform authenticator.
    fun setupPlatformAuthenticator()

    // Registers a user and creates a platform-specific passkey.
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = ""
    ): RegistrationResult

    // Verifies the user against their registered credential.
    suspend fun onAuthenticatorClick(
        appName: String = "",
        savedRegistrationData: String? = null
    ): AuthenticationResult
}
```
`PlatformAuthenticator` (Underlying Engine)
This `expect class` contains the core platform-specific logic. It's managed by the `PlatformAuthenticationProvider`.

```kotlin
expect class PlatformAuthenticator private constructor() {
    constructor(activity: Any? = null)
    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>
    fun setDeviceAuthOption()
    suspend fun registerUser(...): RegistrationResult
    suspend fun authenticate(...): AuthenticationResult
}
```

### Important Note for Android Developers
**Thread Requirement**: The Android `BiometricPrompt` API, which this module uses under the hood, requires that it be invoked from the main thread.

Therefore, you must call `platformAuthenticationProvider.registerUser(...)` and `platformAuthenticationProvider.onAuthenticatorClick(...)` from the **Main dispatcher**. Using `viewModelScope` in Android ViewModels handles this correctly, but it's good practice to be explicit.

```kotlin
// Always launch from the Main thread for these calls
viewModelScope.launch(Dispatchers.Main) {
    // ... call registerUser or onAuthenticatorClick
}
```

### PlatformAuthenticatorStatus (enum)
The `getDeviceAuthenticatorStatus()` function returns a set of the following values:

- `NOT_AVAILABLE` – Platform authenticator is not supported on the device.
- `NOT_SETUP` – Authenticator is available but not set up yet.
- `DEVICE_CREDENTIAL_SET` – Device credential (PIN, password, etc.) is available.
- `BIOMETRICS_NOT_SET` – Biometrics are supported but not configured.
- `BIOMETRICS_NOT_AVAILABLE` – Biometrics are not available on the device.
- `BIOMETRICS_UNAVAILABLE` – Biometrics are temporarily unavailable.
- `BIOMETRICS_SET` – Biometrics are available and configured.

### ViewModel Integration Examples
Here’s how you can integrate `PlatformAuthenticationProvider` into your ViewModels, ensuring calls are made on the correct thread.

**Registration ViewModel**
This ViewModel handles the logic for the user registration screen.

```kotlin
class ChooseAuthOptionScreenViewmodel(
    private val platformAuthenticationProvider: PlatformAuthenticationProvider,
    // other dependencies...
) : ViewModel() {

    private val _registrationResult = MutableStateFlow<RegistrationResult?>(null)
    val registrationResult = _registrationResult.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(platformAuthenticationProvider.deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    fun updatePlatformAuthenticatorStatus() {
        _authenticatorStatus.value = platformAuthenticationProvider.deviceAuthenticatorStatus()
    }

    fun registerUser(userID: String = "", userEmail: String = "", displayName: String = "") {
        // Explicitly launch on the Main thread
        viewModelScope.launch(Dispatchers.Main) {
            _registrationResult.value = platformAuthenticationProvider.registerUser(
                userID,
                userEmail,
                displayName
            )
        }
    }
}
```

**Possible return values:**

- `RegistrationResult.Success` - Its parameter contains the registration data that has to saved. 
The same data is passed as an argument to the `authenticate` function
- `RegistrationResult.Error` - Its parameter contains a message telling what type of error was received.
- `RegistrationResult.PlatformAuthenticatorNotAvailable`
- `RegistrationResult.PlatformAuthenticatorNotSet`


### Authentication ViewModel
This ViewModel manages the authentication flow, such as on a login screen.

```kotlin
class PlatformAuthenticationScreenViewModel(
    private val platformAuthenticationProvider: PlatformAuthenticationProvider,
    private val preferenceDataStore: PreferenceDataStore
) : ViewModel() {

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    fun authenticateUser(appName: String) {
        // Explicitly launch on the Main thread
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = true
            val savedData = preferenceDataStore.getRegistrationData()
            _authenticationResult.value = platformAuthenticationProvider.onAuthenticatorClick(appName, savedData)
            _isLoading.value = false
        }
    }

    fun clearUserRegistrationFromApp() {
        preferenceDataStore.clearData(REGISTRATION_DATA)
    }
}
```

**Possible return values:**
- `AuthenticationResult.Success`
- `AuthenticationResult.Error`
- `AuthenticationResult.UserNotRegistered` - If the user disables the platform authenticator, or in case of Windows Hello, the passkey is deleted or the Authenticator is disabled. The user should be logged out in this case and registered again.

### Setting Up the Authenticator
Prompt the user to set up a device credential or biometric authentication:

```kotlin
authProvider.setupPlatformAuthenticator()
```
On `Android`, it actually redirects users to a screen for setting up a platform authentication method.
On `Windows`, it will only show a message saying `Set up Windows Hello from settings`. Windows Hello
itself shows a similar message in some cases and doesn't redirect users to the setup screen.

### RegistrationResult (sealed interface)
Returned by `authProvider.registerUser()`.

```kotlin
sealed interface RegistrationResult {
    data class Success(val registrationData: String) : RegistrationResult
    data class Error(val message: String) : RegistrationResult
    data object PlatformAuthenticatorNotSet : RegistrationResult
    data object PlatformAuthenticatorNotAvailable : RegistrationResult
}
```

### AuthenticationResult (sealed interface)
Returned by `authProvider.onAuthenticatorClick()`.

```kotlin
sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Error(val message: String) : AuthenticationResult
    data object UserNotRegistered : AuthenticationResult
}
```

