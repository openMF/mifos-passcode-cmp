package com.mifos.passcode.auth.passcode.domain

interface PasscodeRepository {
    fun getSavedPasscode(): String
    fun savePasscode(passcode: String)
    fun clearPasscode()
    fun isPasscodeSet(): Boolean
}