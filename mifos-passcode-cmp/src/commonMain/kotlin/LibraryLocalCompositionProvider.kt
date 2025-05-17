package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator

val LocalAndroidActivity = compositionLocalOf { Any() }
val LocalContextProvider = compositionLocalOf { Any() }
@Composable
fun LocalCompositionProvider(
    activity: Any = LocalAndroidActivity.current,
    context: Any = LocalContextProvider.current,
    platformAuthenticator: PlatformAuthenticator = PlatformAuthenticator(activity = activity),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAndroidActivity provides activity,
        LocalContextProvider provides context,
        LocalPlatformAuthenticator provides platformAuthenticator,
        content = content,
    )
}

val LocalPlatformAuthenticator: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}