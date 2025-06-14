package com.mifos.passcode.sample.chooseAuthOption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.deviceAuth.RegistrationResult
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class ChooseAuthOptionScreenViewmodel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
):ViewModel() {

    private val _registrationResult = Channel<RegistrationResult?>()
    val registrationResult = _registrationResult.receiveAsFlow()

    fun setRegistrationResultNull(){
        _registrationResult.trySend(null)
    }

    fun registerUser(
        platformAuthenticationProvider: PlatformAuthenticationProvider,
        userID: String = "",
        userEmail: String = "",
        displayName: String = ""
    ){
        viewModelScope.launch(Dispatchers.Main) {
            _registrationResult.trySend(
                platformAuthenticationProvider.registerUser(
                    userID,
                    userEmail,
                    displayName
                )
            )
        }
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