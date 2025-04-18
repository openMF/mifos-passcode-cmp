<div align="center">
<img src="https://user-images.githubusercontent.com/37406965/51083189-d5dc3a80-173b-11e9-8ca0-28015e0893ac.png" alt="Android Client" />
	
## Mifos-Passcode-CMP

Mifos-Passcode-CMP is a secure and flexible passcode management library built using Kotlin Multiplatform and Jetpack Compose Multiplatform (CMP). It enables developers to easily integrate passcode-based authentication along with biometric authentication (such as fingerprint or face recognition) into cross-platform applications using a shared codebase.

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

[![PR Checks](https://github.com/openMF/mobile-wallet/actions/workflows/pr-check.yml/badge.svg)](https://github.com/openMF/mifos-passcode-cmp/actions/workflows/pr-check.yml)
[![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)](https://join.slack.com/t/mifos/shared_invite/zt-2wvi9t82t-DuSBdqdQVOY9fsqsLjkKPA)
</div>

---

# ✅ Supported Platforms

| Platform   | Support Status |
|------------|----------------|
| Android    | ✅ Supported   |
| iOS        | ✅ Supported   |
| macOS      | ✅ Supported   |
| Windows 10+| ✅ Supported   |
| Linux      | ✅ Supported   |
| Web        | ✅ Supported   |

---

## 📁 Project Structure

### `mifos-passcode-cmp`

Core library module containing shared and platform-specific implementations:

- **`commonMain/`**
	- Platform-agnostic passcode and biometric logic (ViewModels, shared logic).
- **`<platform>Main/`**
	– Platform-specific implementations for biometric authentication via native interop:
  - `androidMain/`
  - `iosMain/`
  - `macosMain/`
  - `windowsMain/`
  - `linuxMain/`
  - `webMain/`

### `sample`

Cross-platform sample implementation of the passcode screen UI:

- **`commonMain/`** 
	– Shared passcode screen logic using Compose Multiplatform.
- **`<platform>Main/`** 
	– Platform-specific UI wiring for the passcode screen.

---

## For a basic implementation of the PassCode Screen
- Import `PasscodeScreen` to your project which has 4 parameters mentioned below:
  - `onForgotButton`: This will allow to handle the case when the user isn't able to log into the app. In our project we are redirecting the user to login page
  - `onSkipButton`: This offers users the flexibility to bypass the passcode setup process, granting them immediate access to the desired screen
  - `onPasscodeConfirm`: This allows you to pass a function that accepts a string parameter
  - `onPasscodeRejected`: This can be used to handle the event when user has entered a wrong passcode

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
