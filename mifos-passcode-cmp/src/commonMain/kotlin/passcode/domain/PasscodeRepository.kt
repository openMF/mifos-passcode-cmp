package com.mifos.passcode.passcode.domain

interface PasscodeRepository {
    fun getSavedPasscode(): String
    val hasPasscode: Boolean
    fun savePasscode(passcode: String)
    fun clearPasscode()
}