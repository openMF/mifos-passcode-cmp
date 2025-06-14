package com.mifos.passcode.sample.platformAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.chooseAuthOption.REGISTRATION_DATA
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlatformAuthenticationScreenViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val preferenceDataStore: PreferenceDataStore
):ViewModel() {

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun setAuthenticationResultNull(){
        _authenticationResult.value = null
    }

    fun authenticateUser(appName:String, platformAuthenticationProvider: PlatformAuthenticationProvider){
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Main) {
            val savedData = chooseAuthOptionRepository.getRegistrationData()
            _authenticationResult.value = platformAuthenticationProvider.onAuthenticatorClick(appName, savedData)
            _isLoading.value = false
        }
    }

    fun clearUserRegistrationFromApp(){
        preferenceDataStore.clearData(REGISTRATION_DATA)
        chooseAuthOptionRepository.clearAuthOption()
    }
}