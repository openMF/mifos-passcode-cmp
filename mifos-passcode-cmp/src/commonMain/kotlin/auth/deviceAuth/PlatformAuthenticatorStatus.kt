package com.mifos.passcode.auth.deviceAuth

enum class PlatformAuthenticatorStatus{
    NOT_AVAILABLE,
    NOT_SETUP,
    DEVICE_CREDENTIAL_SET,
    BIOMETRICS_NOT_SET,
    BIOMETRICS_NOT_AVAILABLE,
    BIOMETRICS_UNAVAILABLE,
    BIOMETRICS_SET
}