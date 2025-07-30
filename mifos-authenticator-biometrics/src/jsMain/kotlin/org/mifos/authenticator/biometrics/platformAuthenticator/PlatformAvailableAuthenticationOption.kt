package org.mifos.authenticator.biometrics.platformAuthenticator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformAvailableAuthenticationOption private actual constructor() {

    actual constructor(context: Any?) : this() {
        _currentAuthOptions.value = getAuthOption()
    }
    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> = _currentAuthOptions.asStateFlow()



    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = emptyList<PlatformAuthOptions>()

        return availablePlatformAuthOptions
    }
}