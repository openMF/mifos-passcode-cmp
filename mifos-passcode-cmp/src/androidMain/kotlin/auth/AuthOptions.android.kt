package auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.AuthOptions


class AuthOptionAndroid(
    val context: Context
): AuthOption{
    override fun getAuthOption(): List<AuthOptions> {
        val availableAuthOptions = mutableListOf(AuthOptions.UserCredential, AuthOptions.MifosPasscode)

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
        if(face) availableAuthOptions.add(AuthOptions.FaceId)
        if(fingerprint) availableAuthOptions.add(AuthOptions.Fingerprint)
        if(iris) availableAuthOptions.add(AuthOptions.Iris)

        return availableAuthOptions
    }
}
