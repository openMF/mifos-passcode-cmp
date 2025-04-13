package com.mifos.passcode.passcode.data.repository

import com.mifos.passcode.passcode.domain.PasscodeRepository
import com.mifos.passcode.passcode.data.database.PreferenceManager

class PasscodeRepositoryImpl (private val preferenceManager: PreferenceManager) :
    PasscodeRepository {

    override fun getSavedPasscode(): String {
        return preferenceManager.getSavedPasscode()
    }

    override val hasPasscode: Boolean
        get() = preferenceManager.hasPasscode

    override fun savePasscode(passcode: String) {
        preferenceManager.savePasscode(passcode)
    }

    override fun clearPasscode() {
        preferenceManager.clearSavedPasscode()
    }

}