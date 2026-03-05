# Mifos Authenticator Passcode

`mifos-authenticator-passcode` is a Kotlin Multiplatform library that provides a comprehensive and customizable UI for passcode entry, creation, and management, along with the underlying logic. It integrates with Compose Multiplatform to offer a flexible and visually appealing passcode screen for Android, iOS, Desktop, and Web platforms.

## Mifos Passcode Authenticator Integration Guide

This guide explains how to integrate the Mifos Passcode Authenticator into your Compose Multiplatform application.

## Prerequisites

Ensure you have the `io.github.openmf:mifos-authenticator-passcode` library added to your project dependencies.

## Integration Steps

### 1. Implement `PasscodeStorageAdapter`

First, you need to provide an implementation of `PasscodeStorageAdapter` to handle the persistence of the passcode and biometric registration data.

```kotlin
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

class MyPasscodeStorage : PasscodeStorageAdapter {
    override fun savePasscode(passcode: String) {
        // Save passcode to secure storage (e.g., EncryptedSharedPreferences, Keychain, or Settings)
    }

    override fun loadPasscode(): String? {
        // Return the saved passcode or null if not set
        return "saved_passcode" 
    }

    override fun deletePasscode() {
        // Remove the passcode from storage
    }

    override fun saveRegistrationData(data: String) {
        // Save biometric registration data (e.g. public key, credential ID)
    }

    override fun loadRegistrationData(): String? {
        // Load biometric registration data
        return null
    }

    override fun deleteRegistrationData() {
        // Delete biometric registration data
    }
}
```

### 2. Initialize `PasscodeManager`

#### Without dependency injection

In your navigation graph or parent Composable, create an instance of `PasscodeManager` using the provided `rememberPasscodeManager` helper. It handles initialization automatically and ties the manager's lifecycle to the Composable.

```kotlin
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.mifos.authenticator.passcode.rememberPasscodeManager

// Inside your Composable
val scope = rememberCoroutineScope()
val passcodeStorageAdapter = remember { MyPasscodeStorage() } // Or obtained via DI

val passcodeManager = rememberPasscodeManager(
    adapter = passcodeStorageAdapter,
    scope = scope
)
```

#### With dependency injection (e.g. Koin)

When `PasscodeManager` is registered as a **singleton**, it must be initialized exactly once — at creation time inside the DI module.

```kotlin
// DI module
single { PasscodeManager(get(), MainScope()).initialize() }
```

```kotlin
// NavGraph PasscodeScreen entry — inject without re-initializing
val passcodeManager = koinInject<PasscodeManager>()
```

### 3. Setup Navigation

You can now use the `PasscodeManager` to determine the start destination.

```kotlin
val isUsingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank()

val startDestination = if (isUsingPasscode) {
    Route.PasscodeScreen
} else {
    Route.LoginScreen
}
```

### 4. Implement the `PasscodeScreen`

Add the `PasscodeScreen` to your navigation graph. You need to handle several callbacks to define what happens after specific events.

```kotlin
import org.mifos.authenticator.passcode.screen.PasscodeScreen

composable<Route.PasscodeScreen> {
    PasscodeScreen(
        passcodeManager = passcodeManager,
        // Called when the user successfully enters the correct passcode or uses biometrics
        onPasscodeConfirm = {
            navController.popBackStack()
            navController.navigate(Route.HomeScreen)
        },
        // Called when the user taps "Forgot Passcode?"
        onForgotButton = {
            navController.navigate(Route.LoginScreen)
        },
        // Called when a new passcode is successfully created and confirmed
        onPasscodeCreation = {
            navController.navigate(Route.BiometricSetupScreen) // Suggest biometrics after passcode setup
        },
        // Called when an existing passcode is successfully changed
        onPasscodeChanged = {
            navController.popBackStack()
        },
        // Called when the entered passcode is incorrect
        onPasscodeRejected = {
            // e.g. Vibrate device
        },
        // Called when biometrics are successfully disabled
        onDisableBiometrics = {
            navController.popBackStack()
        },
        // Called when a biometric error occurs
        onBiometricError = { message ->
            // Show error message
        },
        // Optional: Provide a custom biometric button component
        biometricButton = { modifier ->
            BiometricKey(modifier, passcodeManager)
        }
    )
}
```

### 5. Biometric Integration

To enable biometrics, you need to use a platform-specific biometric provider (like `mifos-authenticator-biometrics`) and send actions to `PasscodeManager`.

#### Enabling Biometrics:
```kotlin
// After successful biometric registration on the platform:
passcodeManager.trySendAction(PasscodeAction.SaveBiometricRegistration(registrationData))
```

#### Unlocking with Biometrics:
When the user clicks the biometric button on the `PasscodeScreen`:
```kotlin
// In your BiometricKey component:
val result = platformAuthenticator.authenticate(...)
if (result is Success) {
    passcodeManager.trySendAction(PasscodeAction.BiometricUnlockSuccess)
} else {
    passcodeManager.trySendAction(PasscodeAction.BiometricUnlockFailure(result.message))
}
```

## Summary of Key Components

*   **`PasscodeStorageAdapter`**: Interface you must implement for storage logic (Passcode & Biometrics).
*   **`PasscodeManager`**: State holder for the passcode logic.
*   **`PasscodeScreen`**: The UI Composable provided by the library.

### Key `PasscodeAction`s:
- `ChangePasscode`: Initiates the passcode change flow.
- `ForgetPasscode`: Deletes passcode/biometrics and resets to Create mode (emits `OnPasscodeDeletion`).
- `LogOutErase`: Silently erases all security data (no event emitted).
- `DisableBiometrics`: Initiates the flow to disable biometrics (requires passcode verification).
- `UpdatePasscodeLength(length)`: Changes the required passcode length (4 or 6).
- `BiometricUnlockSuccess`: Signals successful biometric authentication.
- `SaveBiometricRegistration(data)`: Saves biometric registration data and enables biometrics in state.

### Key `PasscodeEvent`s:
- `OnUnlockSuccess`: Emitted on successful passcode or biometric entry.
- `OnPasscodeCreateSuccess`: Emitted after initial passcode setup.
- `OnPasscodeChanged`: Emitted after a passcode change.
- `OnPasscodeDeletion`: Emitted after `ForgetPasscode` action.
- `OnRejectEnteredPasscode`: Emitted on incorrect passcode entry.
- `OnBiometricUnlockFailure`: Emitted when biometric unlock fails.

## Customization

The `PasscodeScreen` offers extensive customization through various configuration data classes:

- `PasscodeAppearanceConfig`: General screen background and header text style.
- `PasscodeLogoConfig`: Customize the logo image and size.
- `PasscodeDotConfig`: Control the appearance of the passcode input dots.
- `PasscodeKeyConfig`: Configure the keypad keys (shuffle keys, colors, shape, etc.).
- `PasscodeButtonConfig`: Style the "Forgot Passcode" button.
- `PasscodeSwitchConfig`: Customize the passcode length toggle switch.
- `PasscodeDialogConfig`: Customize the "Passcode Mismatched" dialog appearance.

## Screenshots

## Mobile
|                                       |                                       |                                       
|:-------------------------------------:|:-------------------------------------:|
|<img src= https://github.com/user-attachments/assets/b0ea6771-8bd2-403b-9d32-ae0f12eed847 />| <img src= https://github.com/user-attachments/assets/06415a14-3271-42b7-9ddb-93e0bad529fe />|

## Desktop and web

<img src = https://github.com/user-attachments/assets/fe699e95-d911-46c2-8d96-78e80a90dac6 />
<img src="https://github.com/user-attachments/assets/9c3a3d20-b801-42f0-a345-4bb491f95ce4" />
<img src="https://github.com/user-attachments/assets/bac1d30a-013d-42b1-8b21-bb3dd8a3ea84" />

---

## License

This library is licensed under the Mozilla Public License, v. 2.0. See the [LICENSE](LICENSE) file for more details.
