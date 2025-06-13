package com.mifos.passcode.sample.platformAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.chooseAuthOption.REGISTRATION_DATA
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlatformAuthenticationScreenViewModel(
    private val platformAuthenticationProvider: PlatformAuthenticationProvider,
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption,
    private val preferenceDataStore: PreferenceDataStore
):ViewModel() {

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(platformAuthenticationProvider.deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    private val _availableAuthenticationOption = MutableStateFlow<List<PlatformAuthOptions>>(listOf(PlatformAuthOptions.UserCredential))
    val availableAuthenticationOption = _availableAuthenticationOption.asStateFlow()

    init {
        _availableAuthenticationOption.value =platformAvailableAuthenticationOption.getAuthOption()
    }

    fun updatePlatformAuthenticatorStatus(){
        _authenticatorStatus.value = platformAuthenticationProvider.deviceAuthenticatorStatus()
    }

    fun setAuthenticationResultNull(){
        _authenticationResult.value= null
    }

    fun authenticateUser(appName:String){
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