# Mifos Passcode CMP — Features

A reference for what the passcode library is, why it exists, what it ships today, and what is planned next. This document is the canonical entry point for the project's vision and feature surface.

## Vision

`mifos-authenticator-passcode` is a Kotlin Multiplatform / Compose Multiplatform library that provides a configurable passcode authentication screen and the state machine behind it. It is built for application developers who need an in-app PIN/passcode lock as part of a larger authentication strategy, and who want a single implementation that runs on Android, iOS, Desktop, and Web from one codebase.

The library is guided by four principles:

- **Cross-platform.** A single Kotlin codebase via KMP + Compose Multiplatform. The same `PasscodeScreen` and `PasscodeManager` run on Android, iOS, Desktop (JVM), and Web (Wasm/JS).
- **Storage-agnostic.** The library never touches platform storage directly. Consumers implement a 3-method `PasscodeStorageAdapter` and decide how passcodes are persisted, encrypted, and migrated.
- **Decoupled from biometrics.** The passcode library has zero knowledge of biometrics or any other external authenticator. It exposes a composable slot (`externalAuthButton`) and a `Boolean` flag (`isExternalAuthEnabled`) — nothing more. This lets the library be used standalone, lets consumers plug in any second factor (biometrics, server PIN, hardware token), and keeps the companion biometrics library independently consumable. Neither library imports the other; integration lives in the consumer app.
- **Customizable UI.** Seven `data class` config objects passed to `PasscodeScreen` expose colors, sizes, text styles, and shapes. Defaults work out of the box; visual style is overridable without forking the library. (Layout paddings and spacing between elements are currently fixed in the composable.)

## Features

Every item below is part of the current public API.

### Flows
Source: `mifos-authenticator-passcode/src/commonMain/kotlin/org/mifos/authenticator/passcode/PasscodeManager.kt`

- **Create + confirm a new passcode.** `PasscodeStep.Create` → `PasscodeStep.Confirm`, emitting `PasscodeResult.Created`.
- **Verify passcode for unlock.** `PasscodeStep.Enter`, emitting `PasscodeResult.Verified` on match or `PasscodeResult.Rejected` on mismatch.
- **Change passcode.** `changePasscode()` enters `PasscodeStep.ChangeVerify` (re-enter old) → `PasscodeStep.Create` → `PasscodeStep.Confirm`, emitting `PasscodeResult.Changed` on success. A wrong old passcode at `ChangeVerify` emits `PasscodeResult.Rejected`.
- **Forget passcode from inside the screen.** "Forgot Passcode?" button clears the stored passcode and emits `PasscodeResult.Forgotten`.
- **Log out / clear from elsewhere.** `logOut()` clears stored data and resets to the create flow without emitting any result; the caller drives navigation.

### API shape
Source: `mifos-authenticator-passcode/src/commonMain/kotlin/org/mifos/authenticator/passcode/PasscodeManager.kt`

- **Single-arg constructor:** `PasscodeManager(adapter: PasscodeStorageAdapter)`. Initial step is selected automatically from whether a passcode is already stored.
- **Reactive state:** `state: StateFlow<PasscodeState>` exposes `filledDots`, `passcodeLength`, `passcodeVisible`, `currentPasscodeInput`, `loadedPasscode`, `passcodeStep`, `isChangeFlow`, `shakeAnimationTrigger`.
- **Single result callback:** `onResult: (PasscodeResult) -> Unit` on `PasscodeScreen`. `PasscodeResult` is a sealed interface with five variants: `Verified`, `Created`, `Changed`, `Forgotten`, `Rejected`.
- **Public methods are deliberately minimal:** `changePasscode()` and `logOut()`. UI dispatch (key entry, deletion, visibility toggle, length update) is `internal` and called by `PasscodeScreen` directly — it is not part of the consumer-facing API.

### UI
Source: `mifos-authenticator-passcode/src/commonMain/kotlin/org/mifos/authenticator/passcode/screen/PasscodeScreen.kt`, `screen/PasscodeScreenConfig.kt`

- **Single composable entry point:** `PasscodeScreen` handles the in-screen flows (create + confirm, verify, change, forget) by branching on `PasscodeStep`. `logOut()` is initiated from outside the screen and resets state to the create flow.
- **Configurable length:** 4 or 6 digits via `PasscodeLength.FOUR_DIGIT` / `PasscodeLength.SIX_DIGIT`. Switchable at runtime through an on-screen toggle, rendered only during the `Create` step.
- **Visibility toggle:** show or mask the digits being entered.
- **Mismatch feedback:** counter-driven shake animation plus a `PasscodeMismatchedDialog`. Fires on any failed entry (wrong unlock passcode, wrong old passcode at `ChangeVerify`, mismatched confirmation during creation). Triggered via `PasscodeState.shakeAnimationTrigger`, which is incremented rather than flipped — repeated mismatches always re-fire. `PasscodeResult.Rejected` is emitted only for the first two cases; a failed confirm step is silent and the user simply re-enters.
- **Optional key shuffling:** `PasscodeKeyConfig.shouldShuffleKeys` (default `true`). Shuffling is applied only during the `Enter` step, not while creating or confirming a passcode.
- **Themable via seven config classes** (all `data class`, all defaultable):
  - `PasscodeAppearanceConfig` — background, header text style.
  - `PasscodeLogoConfig` — logo size and painter.
  - `PasscodeDotConfig` — dot color, size, spacing, visible-character text style.
  - `PasscodeKeyConfig` — key text style, color, shape, elevation, container color, size, shuffle behaviour.
  - `PasscodeButtonConfig` — text styles for the action buttons.
  - `PasscodeSwitchConfig` — colors and text style for the length switch.
  - `PasscodeDialogConfig` — colors and shape for the mismatch dialog.
- **External-auth slot:** `externalAuthButton: @Composable ((Modifier) -> Unit)?`. The caller supplies the composable; the library renders it only during `PasscodeStep.Enter` and only when `isExternalAuthEnabled = true`. The library tracks no state for the external authenticator.

### Persistence contract
Source: `mifos-authenticator-passcode/src/commonMain/kotlin/org/mifos/authenticator/passcode/PasscodeStorageAdapter.kt`

- **Three methods:** `savePasscode(String)`, `loadPasscode(): String?`, `deletePasscode()`.
- **The consumer owns persistence.** The library makes no assumption about platform, encryption-at-rest, or schema. Encrypting or hashing the passcode string before storage is the consumer's responsibility — and is the recommended practice today.

## Roadmap

Planned work, tracked in GitHub issues. Each item is independent of the shipped feature surface above.

| Feature | Tracking |
|---|---|
| Zero-knowledge encryption — Argon2id key derivation + AES-256-GCM with per-record salt and IV, GCM auth-tag verification | [#68](https://github.com/openMF/mifos-passcode-cmp/issues/68) |
| Brute-force protection — configurable attempt limit, progressive lockout, persistence across app restarts | [#71](https://github.com/openMF/mifos-passcode-cmp/issues/71) |
| Hardware-backed key wrapping — Android KeyStore + iOS Keychain, defense-in-depth on top of #68 | [#69](https://github.com/openMF/mifos-passcode-cmp/issues/69) |

## Companion library

`mifos-authenticator-biometrics` is the companion library providing platform authenticator integration: Android `BiometricPrompt`, iOS `LocalAuthentication`, Windows Hello via JNA, and WebAuthn for Web/Wasm. It is fully decoupled from the passcode library — neither imports the other. Apps that need both wire them together at the consumer level (see `cmp-sample-shared` for a working example). The biometrics library exposes its own `BiometricStorageAdapter` and registration lifecycle; refer to `mifos-authenticator-biometrics/README.md` for its feature surface.
