# Mifos Authenticator Biometrics

A Kotlin Multiplatform library for device-based authentication (biometrics, Windows Hello, Face ID, device credentials). You supply a storage adapter; the library handles the registration-data lifecycle internally so your UI code never has to save, load, or delete it by hand.

## Installation

Add the `io.github.openmf:mifos-authenticator-biometrics` dependency to your `build.gradle.kts` file.

---

## Quick Start

### 1. Implement `BiometricStorageAdapter`

You provide the storage backend. The library calls these methods — **you do not call them yourself**.

```kotlin
class BiometricStorageAdapterImpl(
    private val settings: Settings,
) : BiometricStorageAdapter {
    override fun saveRegistrationData(registrationData: String) {
        settings.putString("biometric_registration_data", registrationData)
    }

    override fun loadRegistrationData(): String? {
        return settings.getStringOrNull("biometric_registration_data")
    }

    override fun deleteRegistrationData() {
        settings.remove("biometric_registration_data")
    }
}
```

Use `EncryptedSharedPreferences` on Android or Keychain on iOS in production.

### 2. Wrap your app with `PlatformAuthenticatorLocalCompositionProvider`

Pass the adapter once at the composition root. A `PlatformAuthenticationProvider` is built for you and published via `CompositionLocal`.

```kotlin
@Composable
fun App() {
    val biometricStorageAdapter = koinInject<BiometricStorageAdapter>()
    PlatformAuthenticatorLocalCompositionProvider(biometricStorageAdapter) {
        MaterialTheme {
            AppNavigation()
        }
    }
}
```

### 3. Register a user

```kotlin
val authProvider = platformAuthenticationProvider.current
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        val result = authProvider.registerUser(
            userName = "mifosUser123",
            emailId = "user@mifos.org",
            displayName = "Mifos User",
        )
        when (result) {
            is RegistrationResult.Success -> onSuccess()
            is RegistrationResult.Error -> showError(result.message)
            RegistrationResult.UserCancelled -> { }
            RegistrationResult.PlatformAuthenticatorNotSet -> promptUserToSetup()
            RegistrationResult.PlatformAuthenticatorNotAvailable -> showNotAvailableMessage()
        }
    }
}) { Text("Enable Biometrics") }
```

On `Success`, the registration blob is persisted via your adapter automatically. `isRegistered` flips to `true`.

### 4. Authenticate

```kotlin
val authProvider = platformAuthenticationProvider.current
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        when (val result = authProvider.onAuthenticatorClick(appName = "My App")) {
            AuthenticationResult.Success -> onAuthenticated()
            is AuthenticationResult.Error -> showError(result.message)
            AuthenticationResult.UserCancelled -> { }
            AuthenticationResult.UserNotRegistered -> {
                // Library has already cleared the invalid stored blob.
                promptUserToRegister()
            }
        }
    }
}) { Text("Authenticate") }
```

### 5. Unregister / logout

```kotlin
scope.launch { authProvider.unregister() }
```

---

## API Reference

### `PlatformAuthenticationProvider`

Thread-safe façade over the platform authenticator. In Compose code, obtain it via the `platformAuthenticationProvider` CompositionLocal (`platformAuthenticationProvider.current`).

```kotlin
class PlatformAuthenticationProvider(
    authenticator: PlatformAuthenticator,
    biometricStorageAdapter: BiometricStorageAdapter,
) {
    val authenticatorStatus: StateFlow<Set<PlatformAuthenticatorStatus>>
    val isRegistered: StateFlow<Boolean>

    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult

    suspend fun onAuthenticatorClick(appName: String = ""): AuthenticationResult

    suspend fun unregister()

    fun setupPlatformAuthenticator()
}
```

| Method | Side effect |
|---|---|
| `registerUser` | On `Success`, calls `adapter.saveRegistrationData` and sets `isRegistered = true`. |
| `onAuthenticatorClick` | Loads the saved blob via `adapter.loadRegistrationData`. If the platform returns `UserNotRegistered`, calls `adapter.deleteRegistrationData` and sets `isRegistered = false`. |
| `unregister` | Calls `adapter.deleteRegistrationData` and sets `isRegistered = false`. |

You never need to call adapter methods directly from your UI.

### `PlatformAuthenticator` (underlying `expect class`)

```kotlin
expect class PlatformAuthenticator private constructor() {
    constructor(activity: Any? = null)

    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>
    fun setDeviceAuthOption()

    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult

    suspend fun authenticate(
        title: String = "",
        savedRegistrationOutput: String?,
    ): AuthenticationResult
}
```

Normally you don't construct this yourself; `PlatformAuthenticatorLocalCompositionProvider` does it for you (passing the Android `FragmentActivity` when needed).

### `PlatformAuthenticatorStatus`

Returned in `authenticatorStatus`:

- `NOT_AVAILABLE` – Platform authenticator is not supported on the device
- `NOT_SETUP` – Authenticator is available but not configured
- `DEVICE_CREDENTIAL_SET` – Device credential (PIN, password) is configured
- `BIOMETRICS_NOT_SET` – Biometrics are supported but not enrolled
- `BIOMETRICS_NOT_AVAILABLE` – Biometrics are not available on the device
- `BIOMETRICS_UNAVAILABLE` – Biometrics are temporarily unavailable
- `BIOMETRICS_SET` – Biometrics are available and configured

### `RegistrationResult`

```kotlin
sealed interface RegistrationResult {
    data class Success(val message: String) : RegistrationResult
    data class Error(val message: String) : RegistrationResult
    data object UserCancelled : RegistrationResult
    data object PlatformAuthenticatorNotSet : RegistrationResult
    data object PlatformAuthenticatorNotAvailable : RegistrationResult
}
```

### `AuthenticationResult`

```kotlin
sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Error(val message: String) : AuthenticationResult
    data object UserCancelled : AuthenticationResult
    data object UserNotRegistered : AuthenticationResult
}
```

### `BiometricStorageAdapter`

```kotlin
interface BiometricStorageAdapter {
    fun saveRegistrationData(registrationData: String)
    fun loadRegistrationData(): String?
    fun deleteRegistrationData()
}
```

**You implement this, the library calls it.** Don't call these methods from app code.

---

## Platform-Specific Notes

### Android

- Uses `androidx.biometric.BiometricPrompt`
- Requires `FragmentActivity` / `AppCompatActivity` — picked up automatically via `LocalActivity.current` inside `PlatformAuthenticatorLocalCompositionProvider`
- `registerUser` and `onAuthenticatorClick` **must be called from the Main thread**. `rememberCoroutineScope().launch { ... }` inside a composable is safe.

### iOS

- Uses `LocalAuthentication` framework (Face ID / Touch ID, falls back to passcode)
- No special context required

### Windows (Desktop)

- Uses Windows Hello API via JNA + WebAuthn
- Registration is mandatory and produces a credential blob — the library handles save/load/delete for you

### Web (JS / Wasm)

- Uses Web Authentication API (WebAuthn)
- Requires HTTPS in production

---

## Using with Passcode Library

`mifos-authenticator-biometrics` and `mifos-authenticator-passcode` do not import each other. Bridge them in your app via a thin wrapper composable — biometric success is **never** translated into a `PasscodeResult`; it gets its own callback.

### DI setup

```kotlin
val appModule = module {
    single { Settings() }
    singleOf(::PasscodeStorageAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricStorageAdapterImpl).bind<BiometricStorageAdapter>()
    single { PasscodeManager(get<PasscodeStorageAdapter>()) }
}
```

`PasscodeManager` has no knowledge of biometrics — no adapter read at bootstrap, no external-auth flag.

### Build your own biometric button

Keep the button in **your app**, not the library, so the biometrics library stays usable without the passcode library:

```kotlin
@Composable
fun MyBiometricKey(
    modifier: Modifier,
    onSuccess: () -> Unit,
    onUserNotRegistered: () -> Unit,
    onError: (String) -> Unit,
) {
    val authProvider = platformAuthenticationProvider.current
    val scope = rememberCoroutineScope()

    PasscodeKey(
        modifier = modifier,
        keyIcon = Icons.Default.Fingerprint,
        onClick = {
            scope.launch {
                when (val result = authProvider.onAuthenticatorClick("Unlock")) {
                    AuthenticationResult.Success -> onSuccess()
                    is AuthenticationResult.Error -> onError(result.message)
                    AuthenticationResult.UserNotRegistered -> onUserNotRegistered()
                    AuthenticationResult.UserCancelled -> { }
                }
            }
        },
    )
}
```

### Wrap `PasscodeScreen` once

A small wrapper owns the integration so screens above don't have to wire the button manually. Two separate callbacks — one for passcode results, one for biometric success:

```kotlin
@Composable
fun PasscodeScreenWithBiometrics(
    passcodeManager: PasscodeManager,
    onPasscodeResult: (PasscodeResult) -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricError: (String) -> Unit = {},
) {
    val authProvider = platformAuthenticationProvider.current
    val isRegistered by authProvider.isRegistered.collectAsState()

    PasscodeScreen(
        passcodeManager = passcodeManager,
        onResult = onPasscodeResult,
        isExternalAuthEnabled = isRegistered,
        externalAuthButton = { modifier ->
            MyBiometricKey(
                modifier = modifier,
                onSuccess = onBiometricSuccess,
                onUserNotRegistered = { /* no-op; isRegistered flips via provider */ },
                onError = onBiometricError,
            )
        },
    )
}
```

### Use the wrapper at the call site

```kotlin
val authProvider = platformAuthenticationProvider.current
val scope = rememberCoroutineScope()

PasscodeScreenWithBiometrics(
    passcodeManager = passcodeManager,
    onPasscodeResult = { result ->
        when (result) {
            PasscodeResult.Verified -> navController.navigate(HomeScreen)
            PasscodeResult.Created -> navController.navigate(BiometricSetupScreen)
            PasscodeResult.Changed -> navController.navigate(HomeScreen)
            PasscodeResult.Forgotten -> {
                scope.launch { authProvider.unregister() }
                navController.navigate(LoginScreen)
            }
            PasscodeResult.Rejected -> { }
        }
    },
    onBiometricSuccess = { navController.navigate(HomeScreen) },
    onBiometricError = { msg -> showDialog(msg) },
)
```

### Enable / disable / logout

```kotlin
// Enable biometrics
scope.launch {
    authProvider.registerUser("user", "email", "name")
    // isRegistered flips to true automatically on Success
}

// Disable biometrics (direct — no passcode pre-verify)
scope.launch { authProvider.unregister() }

// Logout — clean up both
scope.launch {
    authProvider.unregister()
    passcodeManager.logOut()
}
```

---

## Troubleshooting

### Android: BiometricPrompt not showing
- Ensure the host activity is a `FragmentActivity`, not a plain `Activity`.
- Verify the call happens on the Main thread.
- Check that biometrics or device credentials are set up on the device.

### Windows: registration fails
- Ensure Windows Hello is enabled in system settings.
- The user must have a PIN set up before using Windows Hello.

### Web: authentication not working
- Verify the site is served over HTTPS (required for WebAuthn).
- Check browser compatibility (modern browsers only).

### iOS: Face ID / Touch ID not available
- Check that biometrics are enrolled in iOS Settings.
- Verify the app has `NSFaceIDUsageDescription` in `Info.plist`.
