package com.mifos.passcode.sample.deviceAuth

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.deviceAuth.AuthenticationResult
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.sample.chooseAuthOption.AppLockOption
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


const val REGISTRATION_DATA = "REGISTRATION_DATA"

class PlatformAuthenticationScreenViewModel(
    private val platformAuthenticationProvider: PlatformAuthenticationProvider,
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption,
    private val preferenceDataStore: PreferenceDataStore
):ViewModel() {

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(platformAuthenticationProvider.deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    private val _availableAuthenticationOption = MutableSharedFlow<List<PlatformAuthOptions>>()
    val availableAuthenticationOption = _availableAuthenticationOption.asSharedFlow()

    init {
        viewModelScope.launch {
            _availableAuthenticationOption.emit(platformAvailableAuthenticationOption.getAuthOption())
        }
    }

    private fun updatePlatformAuthenticatorStatus(){
        _authenticatorStatus.value = platformAuthenticationProvider.deviceAuthenticatorStatus()
    }

    fun authenticateUser(appName:String){
        viewModelScope.launch {
            updatePlatformAuthenticatorStatus()
            val savedData = preferenceDataStore.getSavedData(REGISTRATION_DATA, "")
            _authenticationResult.value = platformAuthenticationProvider.onAuthenticatorClick(appName, savedData)
        }
    }

    fun clearUserRegistrationFromApp(){
        preferenceDataStore.clearData(REGISTRATION_DATA)
        chooseAuthOptionRepository.clearAuthOption()
    }
}