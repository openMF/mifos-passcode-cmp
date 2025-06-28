package com.mifos.passcode.sample.passcode

import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import com.mifos.passcode.utility.PasscodeLength

private const val KEY_PASSCODE = "security_passcode"
private const val KEY_PASSCODE_LENGTH = "security_passcode_length"

class PasscodeRepository (
    private val source: PreferenceDataStore,
){
    fun getPasscode(): String {
        return source.getSavedData(KEY_PASSCODE,"")
    }

    fun getPasscodeLength(): PasscodeLength {
        val length = source.getSavedData(KEY_PASSCODE_LENGTH, 4)
        return when(length){
            4 -> PasscodeLength.FOUR_DIGIT
            6 -> PasscodeLength.SIX_DIGIT
            else -> PasscodeLength.FOUR_DIGIT
        }
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

    fun clearPasscodeLength(){
        source.clearData(KEY_PASSCODE_LENGTH)
    }

    fun clearPasscode() {
        source.clearData(KEY_PASSCODE)
    }

    fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}