package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel

val LocalAndroidActivity = compositionLocalOf { Any() }
val LocalCotextProvider = compositionLocalOf { Any() }
@Composable
fun LocalCompositionProvider(
    activity: Any = LocalAndroidActivity.current,
    context: Any = LocalCotextProvider.current,
    platformAuthenticator: PlatformAuthenticator = PlatformAuthenticator(activity = activity),
    platformAuthOption: AuthOption = AuthOption(context = context),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAndroidActivity provides activity,
        LocalPlatformAuthenticator provides platformAuthenticator,
        LocalAuthOption provides platformAuthOption,
        content = content,
    )
}

val LocalPlatformAuthenticator: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}

val LocalAuthOption: ProvidableCompositionLocal<AuthOption> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}