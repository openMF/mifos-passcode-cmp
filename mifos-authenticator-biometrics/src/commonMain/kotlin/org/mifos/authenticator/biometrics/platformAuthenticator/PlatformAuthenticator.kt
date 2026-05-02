/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

/**
 * A platform-agnostic interface for accessing native platform authenticators like biometrics
 * (e.g., fingerprint, face recognition) and device credentials (e.g., PIN, password, pattern).
 *
 * This class abstracts platform-specific implementations (Android, iOS, Windows, etc.) and provides
 * a common interface for:
 * - Checking authenticator availability and status.
 * - Guiding users to set up authentication methods.
 * - Registering user credentials (e.g., creating passkeys).
 * - Verifying users through authentication.
 *
 * Platform-specific implementations are provided in their respective source sets.
 */
expect class PlatformAuthenticator private constructor() {

    /**
     * Initializes the PlatformAuthenticator instance.
     *
     * @param activity A reference to an Android `FragmentActivity` or `AppCompatActivity`.
     * This is required only on Android to initialize platform-specific components.
     * On other platforms (e.g., iOS, Windows), this parameter can be `null`.
     */
    constructor(activity: Any? = null)

    /**
     * Retrieves the current status of the platform's authentication capabilities.
     *
     * This function should be called to determine which authentication options are available
     * and whether they are configured. It is recommended to call this:
     * - On app launch, to initialize the authentication UI.
     * - Before attempting registration or authentication, to ensure prerequisites are met.
     *
     * @return A [Set] of [PlatformAuthenticatorStatus] values representing the current state
     * of supported and configured authentication options on the device.
     */
    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>

    /**
     * Redirects or instructs the user to set up platform authentication if it is not already configured.
     *
     * - On **Android**, this opens the system settings screen for enabling biometrics or device credentials.
     * - On **iOS**, this will prompt the user to set up Face ID or Touch ID.
     * - On **Windows**, this shows a message prompting the user to manually configure
     * Windows Hello via system settings.
     */
    fun setDeviceAuthOption()

    /**
     * Registers the user with the platform authenticator by creating a passkey or credential.
     *
     * This step is mandatory on platforms like **Windows**, where it generates a passkey for the user.
     * The resulting data must be securely saved and reused during authentication. On **Android** and **iOS**,
     * this step may not be necessary if the platform's authentication mechanism does not require explicit registration.
     *
     * @param userName A unique identifier for the user (e.g., "mifosUser12"). If empty, a random
     * Base64-encoded ID will be generated.
     * @param emailId The user's email address. Defaults to `"mifos@mifos.com"` if not provided.
     * @param displayName The user-facing name to associate with the credential (e.g., "Mifos User").
     * Defaults to `"Mifos User"` if empty.
     *
     * @return A [RegistrationResult] containing the outcome of the registration process. If successful,
     * it may include registration data that needs to be stored for future authentication.
     */
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
        title: String = "",
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "",
    ): RegistrationResult

    /**
     * Authenticates the user via the platform authenticator.
     *
     * This function is typically called when the user needs to verify their identity to access the app
     * or a specific feature.
     *
     * Prompt strings ([title], [subtitle], [description], [negativeButtonText]) are passed
     * through to the platform's prompt UI on platforms that support them (currently Android
     * for all four; iOS only honours [title] as `localizedReason`; desktop and web ignore
     * them). The consumer is expected to provide already-localized strings — the library
     * does not bundle translations for biometric prompt text.
     *
     * @param title A title to be displayed in the authentication dialog (required on Android).
     * @param subtitle A subtitle. Android only.
     * @param description A description shown below the subtitle. Android only.
     * @param negativeButtonText Text for the negative button. Android only, and only used when
     *   `DEVICE_CREDENTIAL` is not in the allowed authenticator set.
     * @param savedRegistrationOutput The registration data obtained from the `registerUser()` call.
     * This is required for authentication on platforms like **Windows** and should be securely stored
     * and provided here. It can be `null` on other platforms.
     *
     * @return An [AuthenticationResult] indicating whether the authentication was successful.
     */
    suspend fun authenticate(
        title: String = "",
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "",
        savedRegistrationOutput: String?,
    ): AuthenticationResult
}
