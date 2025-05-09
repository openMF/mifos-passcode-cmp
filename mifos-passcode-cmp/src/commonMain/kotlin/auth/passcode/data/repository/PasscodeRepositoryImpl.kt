package com.mifos.passcode.auth.passcode.data.repository

import com.mifos.passcode.auth.passcode.domain.PasscodeRepository
import com.mifos.passcode.auth.kmpDataStore.PreferenceDataStore

private const val PASSCODE_INFO_KEY = "passcodeInfo"

class PasscodeRepositoryImpl (
    private val source: PreferenceDataStore,
) : PasscodeRepository {

    override fun getPasscode(): String {
        return source.getSavedData(PASSCODE_INFO_KEY,"")
    }

    override fun savePasscode(passcode: String) {
        source.putData(
            PASSCODE_INFO_KEY,
            passcode
        )
    }

    override fun clearPasscode() {
        source.clearData(PASSCODE_INFO_KEY)
    }

    override fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}