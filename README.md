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
| iOS        | ✅ Supported   ||
| macOS      | ✅ Supported   ||
| Windows 10+| ✅ Supported   | ✅ Supported 
| Linux      | ✅ Supported   ||
| Web        | ✅ Supported   ||

---

## 📁 Project Structure

### `mifos-passcode-cmp`

Core library module containing shared and platform-specific implementations:

- **`commonMain/`**
	- Platform-agnostic Platform Authenticator logic and whole passcode logic.
  - `androidMain/`
  	- Biometric Prompt implementation for Platform Authenticator. 
  - `iosMain/`
  	- LocalAuthenticator implementation for Platform Authenticator. 
  - `desktopMain(jvm)/`
	- `windows/`
  		- Windows Hello implementation for Platform Authenticator.
   	- `linux/`
		- LocalAuthenticator implementation for Platform Authenticator.  
     - `macOS/`  
  - `jsMain/`
  	- WebAuthN implementation for using the available FIDO2 or Platform Authenticator.
  - `wasmMain/`
  	- WebAuthN implementation for using the available FIDO2 or Platform Authenticator.

### `sample`

Cross-platform sample implementation of the passcode screen UI and Platform Authenticator:

- **`commonMain/`** 
	– Shared Platform Authenticator using Compose Multiplatform.
	- Logic for using the Passcode implementation.
- **`<platform>Main/`** 
	– Platform-specific UI wiring for the Platform Authenticator.

---

## For a basic implementation of the PassCode Screen
The `PasscodeScreen` is a composable function designed to handle passcode authentication or setup workflows in your app. It is powered by a state-preserving utility `rememberPasscodeSaver`, which manages the current passcode state and provides utility functions for saving and clearing the passcode.

### ✅ How to Use

To use the `PasscodeScreen`, you must first set up a `PasscodeSaver` instance using `rememberPasscodeSaver`.

```kotlin
val passcodeSaver = rememberPasscodeSaver(
    currentPasscode = currentPasscode,
    isPasscodeSet = isPasscodeAlreadySet,
    savePasscode = { passcode -> /* handle saving */ },
    clearPasscode = { /* handle clearing */ }
)
```
Then pass this `passcodeSaver` to the `PasscodeScreen`:

```kotlin
PasscodeScreen(
    passcodeSaver = passcodeSaver,
    onForgotButton = { /* handle forgot passcode */ },
    onSkipButton = { /* handle skip action */ },
    onPasscodeRejected = { /* handle wrong passcode entry */ },
    onPasscodeConfirm = { passcode -> /* handle successful confirmation */ }
)
```
## Parameters

### `PasscodeScreen`

- **`passcodeSaver`** – Required. Handles the passcode input and stores the current state.
- **`onForgotButton`** – Called when the user taps the **"Forgot"** button.
- **`onSkipButton`** – Called when the user taps the **"Skip"** button.
- **`onPasscodeRejected`** – Optional. Called when the entered passcode is wrong.
- **`onPasscodeConfirm`** – Called when the user enters the correct passcode or finishes setting a new one.

### `rememberPasscodeSaver`

- **`currentPasscode`** – The current passcode (if already set).
- **`isPasscodeSet`** – Tells the screen whether the user is verifying an existing passcode or creating a new one.
- **`savePasscode`** – A function that saves the passcode.
- **`clearPasscode`** – A function that clears the saved passcode.

## How it works

- If there's already a passcode, the screen asks the user to enter it and checks if it matches.
- If no passcode is set, the screen helps the user create and confirm a new one.
- The `rememberPasscodeSaver` keeps everything in sync and remembers the state even if the screen recomposes.

## Screenshots

## Mobile
|                                       |                                       |                                       |
|:-------------------------------------:|:-------------------------------------:|:-------------------------------------:|
|<img src=https://github.com/user-attachments/assets/d494e916-c5b7-41e8-b6c7-65417faab75d />| <img src= https://github.com/user-attachments/assets/d8624573-480d-450c-9123-1d694743a49d />|<img src= https://github.com/user-attachments/assets/fd456192-525d-4bae-81ca-25873ea73d1d />|

## Desktop and web

<img src = https://github.com/user-attachments/assets/e95c9056-b512-47c3-94aa-f0d5f7121b5e />
<img src = https://github.com/user-attachments/assets/82e83b54-207c-4418-b5b7-e058ac51a0ab />
<img src = https://github.com/user-attachments/assets/abf004af-0343-46ea-a7ac-e3dc14b8bddf />


---
