package com.mifos.passcode.sample.passcode

import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore

private const val KEY_PASSCODE = "security_passcode"
private const val KEY_PASSCODE_LENGTH = "security_passcode_length"

class PasscodeRepository (
    private val source: PreferenceDataStore,
){
    fun getPasscode(): String {
        return source.getSavedData(KEY_PASSCODE,"")
    }

    fun getPasscodeLength(): Int {
        return source.getSavedData(KEY_PASSCODE_LENGTH, 4)
    }

    fun savePasscode(passcode: String) {
        source.putData(
            KEY_PASSCODE,
            passcode
        )
    }

    fun savePasscodeLength(passcodeLength: Int){
        source.putData(
            KEY_PASSCODE_LENGTH,
            passcodeLength
        )
    }

    fun clearPasscode() {
        source.clearData(KEY_PASSCODE)
    }

    fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}