package com.mifos.passcode.auth.passcode.domain

interface PasscodeRepository {
    fun getPasscode(): String
    fun savePasscode(passcode: String)
    fun clearPasscode()
    fun isPasscodeSet(): Boolean
}