package com.mifos.passcode.sample.passcode

import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore

private const val PASSCODE_INFO_KEY = "mifos.passcode.passcode"
private const val PASSCODE_LENGTH = "mifos.passcode.length"
class PasscodeRepository (
    private val source: PreferenceDataStore,
){
    fun getPasscode(): String {
        return source.getSavedData(PASSCODE_INFO_KEY,"")
    }

    fun getPasscodeLength(): Int {
        val passcodeLength = source.getSavedData(PASSCODE_LENGTH, "")
        return if(passcodeLength.isNotEmpty()) passcodeLength.toInt() else 0
    }

    fun savePasscode(passcode: String) {
        source.putData(
            PASSCODE_INFO_KEY,
            passcode
        )
    }

    fun savePasscodeLength(passcodeLength: Int){
        source.putData(
            PASSCODE_LENGTH,
            passcodeLength.toString()
        )
    }

    fun clearPasscode() {
        source.clearData(PASSCODE_INFO_KEY)
        source.clearData(PASSCODE_LENGTH)
    }

    fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}