package com.mifos.passcode

import com.mifos.passcode.utility.PreferenceManager


class PasscodeRepositoryImpl constructor(private val preferenceManager: PreferenceManager) :
    PasscodeRepository {

    override fun getSavedPasscode(): String {
        return preferenceManager.getSavedPasscode()
    }

    override val hasPasscode: Boolean
        get() = preferenceManager.hasPasscode

    override fun savePasscode(passcode: String) {
        preferenceManager.savePasscode(passcode)
    }
}