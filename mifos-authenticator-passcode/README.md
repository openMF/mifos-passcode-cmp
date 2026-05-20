# Mifos Authenticator Passcode

`mifos-authenticator-passcode` is a Kotlin Multiplatform library that provides a comprehensive and customizable UI for passcode entry, creation, and management, along with the underlying logic. It integrates with Compose Multiplatform to offer a flexible and visually appealing passcode screen for Android, iOS, Desktop, and Web platforms.

## Mifos Passcode Authenticator Integration Guide

This guide explains how to integrate the Mifos Passcode Authenticator into your Compose Multiplatform application.

## Prerequisites

Ensure you have the `io.github.openmf:mifos-authenticator-passcode` library added to your project dependencies.

## Integration Steps

### 1. Implement `PasscodeStorageAdapter`

Provide an implementation of `PasscodeStorageAdapter` to handle passcode persistence.

```kotlin
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

class MyPasscodeStorage : PasscodeStorageAdapter {
    override fun savePasscode(passcode: String) {
        // Save passcode to secure storage (e.g., EncryptedSharedPreferences, Keychain, or Settings)
    }

    override fun loadPasscode(): String? {
        // Return the saved passcode or null if not set
        return null
    }

    override fun deletePasscode() {
        // Remove the passcode from storage
    }
}
```

### 2. Initialize `PasscodeManager`

`PasscodeManager` should be scoped as a singleton via your DI framework since it is used across multiple screens.

```kotlin
// DI module (e.g. Koin)
single { PasscodeManager(adapter = get<PasscodeStorageAdapter>()) }
```

```kotlin
// Inject in any screen
val passcodeManager = koinInject<PasscodeManager>()
```

### 3. Setup Navigation

You can now use the `PasscodeStorageAdapter` to determine the start destination.

```kotlin
val isUsingPasscode = !passcodeStorageAdapter.loadPasscode().isNullOrBlank()

val startDestination = if (isUsingPasscode) {
    Route.PasscodeScreen
} else {
    Route.LoginScreen
}
```

### 4. Implement the `PasscodeScreen`

Add the `PasscodeScreen` to your navigation graph. All outcomes are delivered via a single `onResult` callback.

```kotlin
import org.mifos.authenticator.passcode.screen.PasscodeScreen
import org.mifos.authenticator.passcode.PasscodeResult

composable<Route.PasscodeScreen> {
    PasscodeScreen(
        passcodeManager = passcodeManager,
        onResult = { result ->
            when (result) {
                PasscodeResult.Verified -> navController.navigate(Route.HomeScreen)
                PasscodeResult.Created -> navController.navigate(Route.NextScreen) /* your post-creation destination */
                PasscodeResult.Changed -> navController.navigate(Route.HomeScreen)
                PasscodeResult.Forgotten -> navController.navigate(Route.LoginScreen)
                PasscodeResult.Rejected -> { /* optional: vibrate device */ }
            }
        },
        // Optional: show an external-auth bypass button during passcode entry
        isExternalAuthEnabled = true,
        externalAuthButton = { modifier -> MyExternalAuthButton(modifier) },
    )
}
```

#### Strings & localization

The example above doesn't pass a `strings` argument. The library defaults it to
`defaultPasscodeStrings()` — a `@Composable` factory exported from
`org.mifos.authenticator.passcode` that resolves locale-aware copy from the library's
bundled translations (58 locale variants + an English base; the device's locale picks
the right `values-XX/strings.xml`). Most consumers don't need to do anything here.

To override — match your brand voice, supply a locale the library doesn't bundle, or
draw strings from your own resources — construct your own `PasscodeStrings` and pass
it explicitly:

```kotlin
PasscodeScreen(
    passcodeManager = passcodeManager,
    onResult = onResult,
    strings = myPasscodeStrings,  // wins over the bundled default
)
```

See `l10n-templates/passcode/README.md` for the canonical key reference and override
patterns.

#### Length-switch behaviour

During passcode creation, the 4-digit / 6-digit toggle is automatically hidden once
the user has typed 4+ digits in 6-digit mode. This prevents the user from stranding
their entry buffer above the 4-digit target (the keypad would silently ignore further
input). The toggle reappears once they delete back to ≤ 3 digits, or stays gone if
they complete the 6-digit passcode. No consumer action required.

### 5. External Authentication (Optional)

The passcode library is fully agnostic to any external auth mechanism. It exposes two knobs on `PasscodeScreen`:

- `isExternalAuthEnabled: Boolean` — whether to render the external auth button during `PasscodeStep.Enter`
- `externalAuthButton: @Composable ((Modifier) -> Unit)?` — the button itself, supplied by you

The library does **not** define what "external auth success" means and provides **no** API to mark the user as verified from outside. When your external auth succeeds, handle it on the caller side — typically by navigating away from the passcode screen. The passcode library's `onResult` callback fires only for passcode-driven events (`Verified`, `Created`, `Changed`, `Forgotten`, `Rejected`).

### 6. Common Operations from Other Screens

#### Change Passcode:
```kotlin
// From HomeScreen or SettingsScreen
passcodeManager.changePasscode()
navController.navigate(Route.PasscodeScreen)
// PasscodeScreen will show "Confirm old passcode" → "Create new passcode" → "Confirm new passcode"
// On success, onResult receives PasscodeResult.Changed
```

#### Log Out:
```kotlin
// Clears passcode silently — no PasscodeResult emitted
passcodeManager.logOut()
navController.navigate(Route.LoginScreen)
```

---

## Summary of Key Components

*   **`PasscodeStorageAdapter`**: Interface for passcode persistence.
*   **`PasscodeManager`**: State holder and logic for passcode operations.
*   **`PasscodeScreen`**: The UI composable provided by the library.
*   **`PasscodeResult`**: Sealed interface for all operation outcomes.

### `PasscodeManager` Methods:
- `changePasscode()`: Initiates the passcode change flow.
- `logOut()`: Silently clears the passcode (no result emitted).

### `PasscodeResult` Values:
- `Verified`: Passcode entered correctly.
- `Created`: New passcode created and confirmed.
- `Changed`: Existing passcode changed.
- `Forgotten`: Passcode deleted via "Forgot Passcode?" button.
- `Rejected`: Incorrect passcode entered.

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
