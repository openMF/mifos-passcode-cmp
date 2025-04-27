package com.mifos.passcode.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.mifos.passcode.auth.deviceAuth.data.repository.ChooseAuthOptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class ChooseAuthOptionViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository
): ViewModel() {

    private val _currentAppLock = MutableStateFlow("")
    val currentAppLock = _currentAppLock.asStateFlow()

    init {
        getAppLock()
    }

    fun setAppLock(option: String){
        chooseAuthOptionRepository.setAuthOption(option)
        _currentAppLock.value = option
    }

    fun getAppLock() {
        _currentAppLock.value = chooseAuthOptionRepository.getAuthOption()
    }

    fun clearAppLock()  {
        chooseAuthOptionRepository.clearAuthOption()
        _currentAppLock.value = ""
    }
}