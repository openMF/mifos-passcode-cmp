package auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.PlatformAuthOptions


class AuthOptionAndroid(
    val context: Context
): AuthOption{
    override fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = mutableListOf(PlatformAuthOptions.UserCredential)

        val pm by lazy {
            context.packageManager
        }

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

        return availablePlatformAuthOptions
    }
}
