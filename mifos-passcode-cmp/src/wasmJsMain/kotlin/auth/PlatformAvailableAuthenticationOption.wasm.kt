package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions


actual class PlatformAvailableAuthenticationOption private actual constructor(){
    actual constructor(context: Any?) : this()
    actual fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = emptyList<PlatformAuthOptions>()

        return availablePlatformAuthOptions
    }
}
