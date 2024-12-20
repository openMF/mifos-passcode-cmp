package com.mifos.passcode

interface PasscodeRepository {
    fun getSavedPasscode(): String
    val hasPasscode: Boolean
    fun savePasscode(passcode: String)
}