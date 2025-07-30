package org.mifos.authenticator.sample.platformAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionRepository
import org.mifos.authenticator.sample.chooseAuthOption.REGISTRATION_DATA
import org.mifos.authenticator.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationScreenViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val preferenceDataStore: PreferenceDataStore
) : ViewModel() {

    private val _authenticationResult = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationResult = _authenticationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun setAuthenticationResultNull() {
        _authenticationResult.value = null
    }

    fun authenticateUser(appName: String, platformAuthenticationProvider: PlatformAuthenticationProvider) {
        _isLoading.value = true
        viewModelScope.launch {
            val savedData = chooseAuthOptionRepository.getRegistrationData()
            _authenticationResult.value = platformAuthenticationProvider.onAuthenticatorClick(appName, savedData)
            _isLoading.value = false
        }
    }

    fun clearUserRegistrationFromApp() {
        preferenceDataStore.clearData(REGISTRATION_DATA)
        chooseAuthOptionRepository.clearAuthOption()
    }
}
