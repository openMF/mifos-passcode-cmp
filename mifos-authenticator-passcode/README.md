# Mifos Authenticator Passcode

`mifos-authenticator-passcode` is a Kotlin Multiplatform library that provides a comprehensive and customizable UI for passcode entry, creation, and management, along with the underlying logic. It integrates with Compose Multiplatform to offer a flexible and visually appealing passcode screen for Android, iOS, Desktop, and Web platforms.

## Mifos Passcode Authenticator Integration Guide

This guide explains how to integrate the Mifos Passcode Authenticator into your Compose Multiplatform application.

## Prerequisites

Ensure you have the `io.github.openmf:mifos-authenticator-passcode` library added to your project dependencies.

## Integration Steps

### 1. Implement `PasscodeStorageAdapter`

First, you need to provide an implementation of `PasscodeStorageAdapter` to handle the persistence of the passcode. This adapter allows the library to save, load, and delete the passcode securely.

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

When `PasscodeManager` is registered as a **singleton**, it must be initialized exactly once — at creation time inside the DI module. Do **not** call `.initialize()` in composable injection sites (default parameters, `koinInject()` call sites, or `NavGraph` entries), as this will overwrite any step state set before navigation.

```kotlin
// DI module
single { PasscodeManager(get(), MainScope()).initialize() }
```

```kotlin
// NavGraph PasscodeScreen entry — inject without re-initializing
val passcodeManager = koinInject<PasscodeManager>()
```

If you need the manager to start in `Create` mode (e.g. for first-time passcode setup), call `initialize()` explicitly on the **calling** screen before navigating, not inside the `PasscodeScreen` entry:

```kotlin
// ChooseAuthOptionScreen or equivalent first-time setup entry point
passcodeManager.initialize()
navController.navigate(Route.PasscodeScreen)
```

### 3. Setup Navigation

You can now use the `PasscodeManager` to determine the start destination (e.g., if a passcode is already set, show the passcode screen; otherwise, show the login or home screen).

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
        // Called when the user successfully enters the correct passcode
        onPasscodeConfirm = {
            navController.popBackStack()
            navController.navigate(Route.HomeScreen)
        },
        // Called when the user taps "Forgot Passcode?" (e.g., navigate to Login/Reset flow)
        onForgotButton = {
            navController.navigate(Route.LoginScreen)
        },
        // Called if the user skips the setup (if allowed/applicable)
        onSkipButton = {
            navController.popBackStack()
            navController.navigate(Route.HomeScreen)
        },
        // Called when a new passcode is successfully created and confirmed
        onPasscodeCreation = {
            navController.popBackStack()
            navController.navigate(Route.HomeScreen)
        },
        // Called when the entered passcode is incorrect (optional side-effect)
        onPasscodeRejected = {
            // e.g. Vibrate device
        }
    )
}
```

### 5. Advanced Usage: Changing & Deleting Passcode

You can trigger specific actions on the `PasscodeManager` from other parts of your app, such as a Settings screen.

#### To Change the Passcode:
Trigger the `ChangePasscode` action and navigate to the `PasscodeScreen`. The manager will automatically handle the "Verify Old -> Create New" flow.

```kotlin
import org.mifos.authenticator.passcode.PasscodeAction

// On a "Change Passcode" button click:
passcodeManager.trySendAction(PasscodeAction.ChangePasscode)
navController.navigate(Route.PasscodeScreen)
```

#### To Delete the Passcode ("Forgot Passcode?" flow):

Use `PasscodeAction.ForgetPasscode` from within `PasscodeScreen`. This deletes the stored passcode, resets the manager to `Create` mode, and emits `PasscodeEvent.OnPasscodeDeletion`, which `PasscodeScreen` collects to invoke `onForgotButton`. An active event collector is always present in this context.

```kotlin
// Wired automatically by PasscodeScreen's built-in "Forgot Passcode?" button.
// If triggering manually from within PasscodeScreen:
passcodeManager.trySendAction(PasscodeAction.ForgetPasscode)
```

#### To Erase the Passcode on Logout (outside `PasscodeScreen`):

Use `PasscodeAction.LogOutErasePasscode` from a logout button on a Home or Settings screen. This deletes the passcode directly via the adapter without emitting any event, so no stale event is buffered for the next session. Navigation is handled explicitly by your logout logic.

```kotlin
// On a "Logout" button click (outside PasscodeScreen):
passcodeManager.trySendAction(PasscodeAction.LogOutErasePasscode)
navController.navigate(Route.LoginScreen) { popUpTo(0) }
```

> **Why two separate actions?** `ForgetPasscode` emits `OnPasscodeDeletion` through an unbounded channel. If dispatched while `PasscodeScreen` is not in the back stack (no active collector), the event is buffered and immediately fires the next time the screen opens — sending the user to login before they can interact. `LogOutErasePasscode` avoids this by erasing storage silently with no event.

## Summary of Key Components

*   **`PasscodeStorageAdapter`**: Interface you must implement for storage logic.
*   **`PasscodeManager`**: State holder for the passcode logic.
*   **`PasscodeScreen`**: The UI Composable provided by the library.
*   **`PasscodeAction`**: Actions you can send to the manager. Key actions: `ChangePasscode`, `ForgetPasscode` (from inside `PasscodeScreen`), `LogOutErasePasscode` (from outside `PasscodeScreen`).

## Customization

The `PasscodeScreen` offers extensive customization through various configuration data classes:

- `PasscodeAppearanceConfig`: General screen background and header text style.
- `PasscodeLogoConfig`: Customize the logo image and size.
- `PasscodeDotConfig`: Control the appearance of the passcode input dots (color, size, spacing, visible text style).
- `PasscodeKeyConfig`: Configure the keypad keys (shuffle keys, text style, colors, shape, elevation, size).
- `PasscodeButtonConfig`: Style the "Skip" and "Forgot Passcode" buttons.
- `PasscodeSwitchConfig`: Customize the passcode length toggle switch.
- `PasscodeToolbarConfig`: Style the optional toolbar indicators (if you enable and implement `PasscodeToolbar`).
- `PasscodeDialogConfig`: Customize the "Passcode Mismatched" dialog appearance.

By providing instances of these configuration classes to the `PasscodeScreen` composable, you can seamlessly integrate the passcode UI into your application's design system.


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
