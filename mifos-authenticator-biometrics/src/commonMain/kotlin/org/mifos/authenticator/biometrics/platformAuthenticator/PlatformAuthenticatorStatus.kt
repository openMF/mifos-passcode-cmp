package org.mifos.authenticator.biometrics.platformAuthenticator

/**
 * Represents the status of platform-based authenticators like biometrics (fingerprint/face) or device
 * credentials (PIN, password, pattern).
 */
enum class PlatformAuthenticatorStatus {

    /**
     * The platform does not support any form of authentication like biometrics or device credentials.
     * This typically means the device lacks hardware support or the required APIs are unavailable.
     */
    NOT_AVAILABLE,

    /**
     * Authentication options (biometrics or device credentials) are supported by the platform
     * but not configured by the user.
     * The user has not enrolled any credentials like PIN, password, fingerprint, or face data.
     */
    NOT_SETUP,

    /**
     * The user has set up device credentials such as a PIN, password, or pattern, which can be used for authentication.
     * This indicates a fallback method is available even if biometrics are not configured.
     */
    DEVICE_CREDENTIAL_SET,

    /**
     * The platform supports biometrics, but the user has not enrolled any biometric credentials yet.
     * This includes scenarios where the device has a fingerprint or face sensor, but no fingerprints
     * or face data are added.
     */
    BIOMETRICS_NOT_SET,

    /**
     * The device does not support biometric authentication.
     * This could be due to missing biometric hardware or platform-level restrictions.
     */
    BIOMETRICS_NOT_AVAILABLE,

    /**
     * Biometric authentication is temporarily unavailable.
     * This can happen when too many failed attempts have been made or the biometric hardware is temporarily locked out.
     */
    BIOMETRICS_UNAVAILABLE,

    /**
     * Biometric authentication is supported and at least one biometric
     * (e.g., fingerprint or face) has been enrolled by the user.
     */
    BIOMETRICS_SET
}
