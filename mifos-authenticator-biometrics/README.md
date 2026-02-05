# Mifos Authenticator Biometrics

`mifos-authenticator-biometrics` is a Kotlin Multiplatform library that provides a unified API for biometric authentication (e.g., fingerprint, face ID) and device credentials (e.g., PIN, password) across Android, iOS, and Windows platforms. It simplifies the process of integrating platform-specific authentication mechanisms into your application, allowing you to write a single codebase for user authentication.

## Features

- **Cross-Platform:** Works on Android, iOS, and Windows.
- **Unified API:** A simple and consistent API for all platforms.
- **Biometric and Device Credentials:** Supports both biometric authentication and device credentials.
- **Reactive State:** Provides a reactive `StateFlow` to observe the status of the platform authenticator.
- **Easy to Use:** Designed to be easy to integrate into your existing projects.

## Getting Started

### Adding the Dependency

To use the library in your project, add the following dependency to your `build.gradle.kts` file:

```kotlin
implementation(projects.mifosAuthenticatorBiometrics)
```

### Usage

The main entry point for the library is the `PlatformAuthenticationProvider` class. It provides methods for checking the authenticator status, registering a user, and authenticating a user.

#### 1. Initialize the Provider

First, create an instance of `PlatformAuthenticationProvider`. On Android, you need to pass a `FragmentActivity` to the constructor. On other platforms, you can pass `null`.

```kotlin
// On Android
val authProvider = PlatformAuthenticationProvider(activity = this)

// On other platforms
val authProvider = PlatformAuthenticationProvider()
```

#### 2. Check the Authenticator Status

You can observe the `authenticatorStatus` `StateFlow` to get real-time updates on the availability and configuration of the platform authenticator.

```kotlin
val authenticatorStatus by authProvider.authenticatorStatus.collectAsState()

if (authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)) {
    // Biometrics are set up and ready to use
} else if (authenticatorStatus.contains(PlatformAuthenticatorStatus.NOT_SETUP)) {
    // The user has not set up any authentication methods yet
    authProvider.setupPlatformAuthenticator()
}
```

#### 3. Register a User

On some platforms, like Windows, you need to register the user before you can authenticate them. This creates a passkey that is used for authentication.

```kotlin
val registrationResult = authProvider.registerUser(
    userName = "mifosUser123",
    emailId = "user@mifos.org",
    displayName = "Mifos User"
)

when (registrationResult) {
    is RegistrationResult.Success -> {
        // Save the registration data for later use
        val registrationData = registrationResult.message
    }
    is RegistrationResult.Error -> {
        // Handle the error
    }
    // ...
}
```

#### 4. Authenticate a User

To authenticate a user, call the `onAuthenticatorClick` method. On platforms that require registration, you need to provide the saved registration data.

```kotlin
val authenticationResult = authProvider.onAuthenticatorClick(
    appName = "My App",
    savedRegistrationData = registrationData // Can be null on Android and iOS
)

when (authenticationResult) {
    is AuthenticationResult.Success -> {
        // Authentication successful
    }
    is AuthenticationResult.Error -> {
        // Handle the error
    }
    // ...
}
```

## Platform-Specific Implementations

The library provides platform-specific implementations for Android, iOS, and Windows.

### Android

On Android, the library uses the `androidx.biometric` library to provide biometric authentication. It supports fingerprint and face authentication, as well as device credentials (PIN, password, pattern).

### iOS

On iOS, the library uses the `LocalAuthentication` framework to provide Face ID and Touch ID authentication.

### Windows

On Windows, the library uses the Windows Hello API to provide biometric authentication. It uses a native C library to interact with the Windows Hello API.

## License

This library is licensed under the Mozilla Public License, v. 2.0. See the [LICENSE](LICENSE) file for more details.
