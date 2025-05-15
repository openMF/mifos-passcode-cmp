package com.mifos.passcode.auth.deviceAuth.old

import com.mifos.passcode.auth.deviceAuth.AuthenticationResult

interface BioMetricUtil {
    suspend fun setAndReturnPublicKey(): String?
    suspend fun authenticate(): AuthenticationResult
    fun canAuthenticate(): Boolean
    fun generatePublicKey(): String?
    fun signUserId(ucc: String): String
    fun isBiometricSet(): Boolean
    fun getPublicKey(): String?
    fun isValidCrypto(): Boolean
}