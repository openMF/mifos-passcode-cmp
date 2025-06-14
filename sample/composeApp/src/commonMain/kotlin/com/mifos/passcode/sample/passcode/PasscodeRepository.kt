package com.mifos.passcode.sample.passcode

import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore

private const val PASSCODE_INFO_KEY = "passcodeInfo"

class PasscodeRepository (
    private val source: PreferenceDataStore,
){
    fun getPasscode(): String {
        return source.getSavedData(PASSCODE_INFO_KEY,"")
    }

    fun savePasscode(passcode: String) {
        source.putData(
            PASSCODE_INFO_KEY,
            passcode
        )
    }

    fun clearPasscode() {
        source.clearData(PASSCODE_INFO_KEY)
    }

    fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}