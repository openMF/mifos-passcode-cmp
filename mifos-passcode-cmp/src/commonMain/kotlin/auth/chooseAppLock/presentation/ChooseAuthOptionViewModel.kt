package com.mifos.passcode.auth.chooseAppLock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.passcode.auth.chooseAppLock.data.repository.ChooseAuthOptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChooseAuthOptionViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository
): ViewModel() {

    private val _currentAppLock = MutableStateFlow("")
    val currentAppLock = _currentAppLock.asStateFlow()

    init {
        getAppLock()
    }

    fun setAppLock(option: String){
        viewModelScope.launch {
            chooseAuthOptionRepository.setAuthOption(option)
            _currentAppLock.value = option
        }
    }

    private fun getAppLock() {
        viewModelScope.launch {
            _currentAppLock.value = chooseAuthOptionRepository.getAuthOption()
        }
    }

    fun clearAppLock(){
        chooseAuthOptionRepository.clearAuthOption()
        _currentAppLock.value =""
    }

}