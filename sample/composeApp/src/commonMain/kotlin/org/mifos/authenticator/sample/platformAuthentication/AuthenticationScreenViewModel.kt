package org.mifos.authenticator.sample.platformAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionRepository
import org.mifos.authenticator.sample.chooseAuthOption.REGISTRATION_DATA
import org.mifos.authenticator.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationScreenViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val preferenceDataStore: PreferenceDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticationResultScreenState())
    val state = _state.asStateFlow()

    private val _event = Channel<AuthenticationResultScreenEvent>()
    val eventFLow = _event.receiveAsFlow()

    fun handleAction(action: AuthenticationResultScreenAction) {
        when(action){
            is AuthenticationResultScreenAction.OnVerifyUser -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                    val savedData = chooseAuthOptionRepository.getRegistrationData()
                    val result = action.platformAuthenticationProvider.onAuthenticatorClick(
                        action.appName,
                        savedData
                    )
                    _state.update {
                        it.copy(
                            false,
                            result
                        )
                    }
                }
            }

            is AuthenticationResultScreenAction.OnClearUserRegistrationData -> {
                clearUserRegistrationFromApp()
                _event.trySend(AuthenticationResultScreenEvent.OnRegistrationDataCleared)
            }
        }
    }

    private fun clearUserRegistrationFromApp() {
        preferenceDataStore.clearData(REGISTRATION_DATA)
        chooseAuthOptionRepository.clearAuthOption()
    }
}


data class AuthenticationResultScreenState(
    val isLoading: Boolean = false,
    val authenticationResult: AuthenticationResult? = null
)

sealed class AuthenticationResultScreenEvent {
    data object OnRegistrationDataCleared : AuthenticationResultScreenEvent()
}

sealed class AuthenticationResultScreenAction {
    data class OnVerifyUser(
        val appName: String,
        val platformAuthenticationProvider: PlatformAuthenticationProvider
    ) : AuthenticationResultScreenAction()

    data object OnClearUserRegistrationData : AuthenticationResultScreenAction()
}


