package com.mifos.passcode.auth

enum class AuthOptions{
    FaceId,
    Fingerprint,
    Iris,
    UserCredential,
    MifosPasscode;
}

interface AuthOption{
    fun getAuthOption(): List<AuthOptions>
}