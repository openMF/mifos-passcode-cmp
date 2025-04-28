package com.mifos.passcode.auth.passcode.data.repository

import auth.preferenceDataStore.PreferenceDataStore
import com.mifos.passcode.auth.passcode.domain.PasscodeRepository
import com.mifos.passcode.utility.Constants


class PasscodeRepositoryImpl (private val preferenceDataStore: PreferenceDataStore) : PasscodeRepository {

    override fun getSavedPasscode(): String {
        return preferenceDataStore.getSavedData(Constants.PASSCODE_KEY, "")
    }

    override fun savePasscode(passcode: String) {
        preferenceDataStore.putData(Constants.PASSCODE_KEY, passcode)
    }

    override fun clearPasscode() {
        preferenceDataStore.clearData(Constants.PASSCODE_KEY)
    }

    override fun isPasscodeSet(): Boolean = getSavedPasscode().isNotBlank()

}