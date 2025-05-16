package com.mifos.passcode.auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions


actual class PlatformAvailableAuthenticationOption actual constructor(){
    private var context: Context? = null
    actual constructor(context: Any?) : this() {
        this.context = context as? Context
    }

    actual fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = mutableListOf(PlatformAuthOptions.UserCredential)

        context?.let {
            val pm = it.packageManager

            var face = false
            var fingerprint = false
            var iris = false

            fingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
                face = pm.hasSystemFeature(PackageManager.FEATURE_FACE)
                iris = pm.hasSystemFeature(PackageManager.FEATURE_IRIS)
            }
            if(face) availablePlatformAuthOptions.add(PlatformAuthOptions.FaceId)
            if(fingerprint) availablePlatformAuthOptions.add(PlatformAuthOptions.Fingerprint)
            if(iris) availablePlatformAuthOptions.add(PlatformAuthOptions.Iris)

            println("Does the device have fingerprint lock? $fingerprint")
            println("Does the device have face lock? $face")
            println("Does the device have iris lock? $iris")
        }

        return availablePlatformAuthOptions
    }
}

