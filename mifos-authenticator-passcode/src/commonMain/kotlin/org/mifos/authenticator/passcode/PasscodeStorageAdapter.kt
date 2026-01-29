package org.mifos.authenticator.passcode


interface PasscodeStorageAdapter {
    fun savePasscode(passcode: String)

    fun loadPasscode(): String?

    fun deletePasscode()
}