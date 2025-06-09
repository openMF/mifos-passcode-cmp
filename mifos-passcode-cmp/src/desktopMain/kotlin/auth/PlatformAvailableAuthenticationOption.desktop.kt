package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.windows.utils.isWindowsTenOrEleven
import com.mifos.passcode.getPlatform
import com.sun.jna.Platform

actual class PlatformAvailableAuthenticationOption private actual constructor(){

    actual constructor(context: Any?) : this()

    val isWindowsTenOrHigh = if(Platform.isWindows()) isWindowsTenOrEleven()  else false
    actual fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = if(isWindowsTenOrHigh){
            listOf(
                PlatformAuthOptions.UserCredential,
                PlatformAuthOptions.Fingerprint,
                PlatformAuthOptions.FaceId,
                PlatformAuthOptions.Voice
            )
        }else emptyList()

        return availablePlatformAuthOptions
    }
}
