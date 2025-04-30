package com.mifos.passcode.auth.passcode.domain

interface PasscodeRepository {
    suspend fun getPasscode(): String
    suspend fun savePasscode(passcode: String)
    suspend fun clearPasscode()
    suspend fun isPasscodeSet(): Boolean
}