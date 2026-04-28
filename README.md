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

## Documentation

- [`FEATURES.md`](FEATURES.md) — passcode library vision and features (start here)


