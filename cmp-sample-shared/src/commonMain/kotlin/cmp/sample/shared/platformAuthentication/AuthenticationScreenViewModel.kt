/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package cmp.sample.shared.platformAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionRepository
import cmp.sample.shared.chooseAuthOption.REGISTRATION_DATA
import cmp.sample.shared.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider

class AuthenticationScreenViewModel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
    private val preferenceDataStore: PreferenceDataStore,
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
