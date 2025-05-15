package com.mifos.passcode.sample.authentication.passcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PASSCODE_INFO_KEY = "passcodeInfo"

class PasscodeViewmodel (
    private val source: PreferenceDataStore,
): ViewModel() {

    private val _currentPasscode = MutableStateFlow("")
    val currentPasscode = _currentPasscode.asStateFlow()

    init {
        getPasscode()
    }

    private fun getPasscode() {
        viewModelScope.launch {
            _currentPasscode.value = source.getSavedData(PASSCODE_INFO_KEY,"")
        }
    }

    fun savePasscode(passcode: String) {
        viewModelScope.launch {
            _currentPasscode.value = passcode
            source.putData(
                PASSCODE_INFO_KEY,
                passcode
            )
        }
    }

    fun clearPasscode() {
        source.clearData(PASSCODE_INFO_KEY)
    }

    fun isPasscodeSet(): Boolean = _currentPasscode.value.isNotBlank()

}