package org.mifos.authenticator.biometrics.platformAuthenticator

import org.mifos.authenticator.biometrics.windows.utils.isWindowsTenOrEleven
import com.sun.jna.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformAvailableAuthenticationOption private actual constructor() {

    actual constructor(context: Any?) : this() {
        _currentAuthOptions.value = getAuthOption()
    }

    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> = _currentAuthOptions.asStateFlow()

    private val isWindowsTenOrHigh = if (Platform.isWindows()) isWindowsTenOrEleven() else false
    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = if (isWindowsTenOrHigh) {
            listOf(
                PlatformAuthOptions.UserCredential,
                PlatformAuthOptions.Fingerprint,
                PlatformAuthOptions.FaceId,
                PlatformAuthOptions.Voice
            )
        } else {
            emptyList()
        }

        return availablePlatformAuthOptions
    }
}