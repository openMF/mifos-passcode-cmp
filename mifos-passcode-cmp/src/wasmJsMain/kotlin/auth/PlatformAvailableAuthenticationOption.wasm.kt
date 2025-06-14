package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


actual class PlatformAvailableAuthenticationOption private actual constructor(){
    actual constructor(context: Any?) : this()

    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> = _currentAuthOptions.asStateFlow()

    init{
        _currentAuthOptions.value = getAuthOption()
    }

    actual fun updateCurrentAuthOption(){
        _currentAuthOptions.tryEmit(getAuthOption())
    }

    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = emptyList<PlatformAuthOptions>()

        return availablePlatformAuthOptions
    }
}
