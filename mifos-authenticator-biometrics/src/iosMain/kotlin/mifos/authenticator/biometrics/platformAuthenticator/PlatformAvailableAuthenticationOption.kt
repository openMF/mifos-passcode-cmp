package mifos.authenticator.biometrics.platformAuthenticator

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSError
import platform.LocalAuthentication.LABiometryTypeFaceID
import platform.LocalAuthentication.LABiometryTypeOpticID
import platform.LocalAuthentication.LABiometryTypeTouchID
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

actual class PlatformAvailableAuthenticationOption private actual constructor() {

    actual constructor(context: Any?) : this() {
        _currentAuthOptions.value = getAuthOption()
    }

    private val _currentAuthOptions = MutableStateFlow<List<PlatformAuthOptions>>(emptyList())
    actual val currentAuthOption: StateFlow<List<PlatformAuthOptions>> =
        _currentAuthOptions.asStateFlow()

    @OptIn(ExperimentalForeignApi::class)
    private fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = mutableListOf<PlatformAuthOptions>()

        val context = LAContext()

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val canEvaluate = context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error.ptr
            )

            if (canEvaluate) {
                when (context.biometryType) {
                    LABiometryTypeFaceID -> availablePlatformAuthOptions.add(PlatformAuthOptions.FaceId)
                    LABiometryTypeTouchID -> availablePlatformAuthOptions.add(PlatformAuthOptions.Fingerprint)
                    LABiometryTypeOpticID -> availablePlatformAuthOptions.add(PlatformAuthOptions.Iris)
                }
            }
        }

        return availablePlatformAuthOptions
    }

}