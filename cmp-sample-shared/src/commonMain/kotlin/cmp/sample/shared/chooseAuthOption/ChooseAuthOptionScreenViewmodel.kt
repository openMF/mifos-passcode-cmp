/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package cmp.sample.shared.chooseAuthOption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult

class ChooseAuthOptionScreenViewmodel(
    private val chooseAuthOptionRepository: ChooseAuthOptionRepository,
) : ViewModel() {

    private val _registrationResult = MutableStateFlow<RegistrationResult?>(null)
    val registrationResult = _registrationResult.asStateFlow()

    fun setRegistrationResultNull() {
        _registrationResult.value = null
    }

    fun registerUser(
        platformAuthenticationProvider: PlatformAuthenticationProvider,
        userID: String = "",
        userEmail: String = "",
        displayName: String = "",
    ) {
        viewModelScope.launch {
            _registrationResult.value = platformAuthenticationProvider.registerUser(
                userID,
                userEmail,
                displayName,
            )
        }
    }

    fun saveRegistrationData(registrationData: String) =
        chooseAuthOptionRepository.saveRegistrationData(registrationData)

    fun clearRegistrationData() = chooseAuthOptionRepository.clearRegistrationData()

    fun getRegistrationData() = chooseAuthOptionRepository.getRegistrationData()

    fun saveAppLockOption(appLock: AppLockOption) {
        chooseAuthOptionRepository.setAuthOption(appLock)
    }

    fun getAppLock(): AppLockOption = chooseAuthOptionRepository.getAuthOption()

    fun clearAppLock() = chooseAuthOptionRepository.clearAuthOption()
}
