package com.mifos.passcode.sample.chooseAuthOption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.deviceAuth.RegistrationResult
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChooseAuthOptionScreenViewmodel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption,
    private val platformAuthenticationProvider: PlatformAuthenticationProvider
):ViewModel() {

    private val _registrationResult = MutableStateFlow<RegistrationResult?>(null)
    val registrationResult = _registrationResult.asStateFlow()

    private val _authenticatorStatus = MutableStateFlow(platformAuthenticationProvider.deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    private val _availableAuthenticationOption = MutableStateFlow(
        platformAvailableAuthenticationOption.getAuthOption()
    )

    init {
        _authenticatorStatus.value = platformAuthenticationProvider.deviceAuthenticatorStatus()
    }

    fun updatePlatformAuthenticatorStatus(){
        _authenticatorStatus.value = platformAuthenticationProvider.deviceAuthenticatorStatus()
    }

    fun setRegistrationResultNull(){
        _registrationResult.value= null
    }

    fun registerUser(
        userID: String = "",
        userEmail: String = "",
        displayName: String = ""
    ){
        viewModelScope.launch(Dispatchers.Main) {
            _registrationResult.value = platformAuthenticationProvider.registerUser(
                userID,
                userEmail,
                displayName
            )
        }
    }

    fun setupPlatformAuthenticator(){
        platformAuthenticationProvider.setupPlatformAuthenticator()
    }

    fun saveRegistrationData(registrationData: String) = chooseAuthOptionRepository.saveRegistrationData(registrationData)

    fun clearRegistrationData() = chooseAuthOptionRepository.clearRegistrationData()

    fun getRegistrationData() = chooseAuthOptionRepository.getRegistrationData()

    fun saveAppLockOption(appLock: AppLockOption){
        chooseAuthOptionRepository.setAuthOption(appLock)
    }

    fun getAppLock(): AppLockOption = chooseAuthOptionRepository.getAuthOption()

    fun clearAppLock() = chooseAuthOptionRepository.clearAuthOption()

}