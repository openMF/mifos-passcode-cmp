package com.mifos.passcode.sample.authentication.passcode

interface PasscodeRepository {
    suspend fun getPasscode(): String
    suspend fun savePasscode(passcode: String)
    fun clearPasscode()
    fun isPasscodeSet(): Boolean
}