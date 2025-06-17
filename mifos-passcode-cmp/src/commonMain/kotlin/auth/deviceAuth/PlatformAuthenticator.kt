package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult


/**
 * A platform-agnostic interface for accessing native platform authenticators like biometrics
 * (e.g., fingerprint, face recognition) and device credentials (e.g., PIN, password, pattern).
 *
 * This class abstracts platform-specific implementations (Android, Windows, etc.) and provides
 * a common interface for checking authenticator availability, guiding users to set up authentication,
 * registering user credentials, and verifying users through authentication.
 *
 * Actual platform implementations are expected in their respective source sets.
 */
expect class PlatformAuthenticator private constructor() {

    /**
     * Initializes the PlatformAuthenticator instance.
     *
     * @param activity A reference to an Android `FragmentActivity` or `AppCompatActivity`.
     * Required only on Android to initialize platform authenticator related components.
     * On other platforms (e.g., Windows), this can be safely passed as null.
     */
    constructor(activity: Any? = null)

    /**
     * Retrieves the current status of the platform's authentication capabilities.
     *
     * @return A [Set] of [PlatformAuthenticatorStatus] values representing the supported
     * and configured authentication options on the device.
     *
     * This function should be invoked:
     * - When the app is launched, to determine available authentication options.
     * - Before attempting registration or authentication, to ensure prerequisites are met.
     */
    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>

    /**
     * Redirects or instructs the user to set up platform authentication if not already configured.
     *
     * - On **Android**, this opens the appropriate system settings screen for enabling
     * biometrics or device credentials.
     * - On **Windows**, this shows a message prompting the user to manually configure
     * Windows Hello via system settings. (Open Settings app your self and setup Windows Hello)
     */
    fun setDeviceAuthOption()

    /**
     * Registers the user with the platform authenticator by creating a passkey or credential.
     *
     * - On **Windows**, this step is mandatory and generates a passkey for the user.
     *   The resulting data must be securely saved and reused during authentication.
     * - On **Android** this function can be skipped for now because it directly calls authenticate function
     *   and uses its implementation.
     * @param userName A unique identifier for the user (eg: mifosUser12). If empty, a random Base64-encoded ID
     * will be generated.
     * @param emailId The user's email address. Defaults to `"mifos@mifos.com"` if not provided.
     * @param displayName The user-facing name to associate with the credential. Defaults to `"Mifos User"` if empty.
     *
     * @return A [RegistrationResult] containing success or error state, along with registration data if successful.
     */
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult

    /**
     * Authenticates the user via platform authenticator using stored registration data.
     *
     * This function is typically called when the user opens the app and needs to verify their identity.
     *
     * @param title A title shown in the authentication dialog. Required on Android.
     * @param savedRegistrationOutput The registration data received during the `registerUser()` call.
     * This must be securely stored and reused for successful authentication on Windows. It can be 
     * `null` on all other platforms.
     *
     * @return An [AuthenticationResult] indicating success or failure.
     */
    suspend fun authenticate(
        title: String = "",
        savedRegistrationOutput: String?
    ): AuthenticationResult
}
