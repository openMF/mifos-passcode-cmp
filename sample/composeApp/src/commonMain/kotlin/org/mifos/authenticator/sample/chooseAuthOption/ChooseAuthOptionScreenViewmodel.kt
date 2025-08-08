package org.mifos.authenticator.sample.chooseAuthOption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import kotlinx.coroutines.launch


class ChooseAuthOptionScreenViewmodel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChooseAuthOptionUiState())

    val state =_state.asStateFlow()

    fun handleAction(action: ChooseAuthOptionUiActions) {
        when(action) {
            is ChooseAuthOptionUiActions.OnDeviceLockSelected -> {
                viewModelScope.launch {

                    _state.update{
                        it.copy(isLoading = true)
                    }

                    val result = action.platformAuthenticationProvider.registerUser(
                        action.userID,
                        action.userEmail,
                        action.displayName
                    )

                    _state.update{
                        it.copy(
                            isLoading = false,
                            registrationResult = result
                        )
                    }
                }
            }
            ChooseAuthOptionUiActions.OnPasscodeSelected -> {
                saveAppLockOption(AppLockOption.MifosPasscode)
            }
            is ChooseAuthOptionUiActions.SaveRegistrationData -> {
                saveRegistrationData(action.message)
                saveAppLockOption(AppLockOption.DeviceLock)
            }
        }

    }

    private fun saveRegistrationData(registrationData: String) =
        chooseAuthOptionRepository.saveRegistrationData(registrationData)

    private fun saveAppLockOption(appLock: AppLockOption) {
        chooseAuthOptionRepository.setAuthOption(appLock)
    }
}


data class ChooseAuthOptionUiState(
    val isLoading: Boolean = false,
    val registrationResult: RegistrationResult?=  null
)

sealed class ChooseAuthOptionUiActions() {
    data class OnDeviceLockSelected(
        val platformAuthenticationProvider: PlatformAuthenticationProvider,
        val userID: String,
        val userEmail: String,
        val displayName: String
    ): ChooseAuthOptionUiActions()
    data object OnPasscodeSelected: ChooseAuthOptionUiActions()
    data class SaveRegistrationData(val message: String): ChooseAuthOptionUiActions()
}
