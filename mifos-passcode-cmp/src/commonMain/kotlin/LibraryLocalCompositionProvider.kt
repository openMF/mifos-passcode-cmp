package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator

val LocalAndroidActivity = compositionLocalOf { Any() }
val LocalCotextProvider = compositionLocalOf { Any() }
@Composable
fun LocalCompositionProvider(
    activity: Any = LocalAndroidActivity.current,
    context: Any = LocalCotextProvider.current,
    platformAuthenticator: PlatformAuthenticator = PlatformAuthenticator(activity = activity),
    platformPlatformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption = PlatformAvailableAuthenticationOption(context = context),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAndroidActivity provides activity,
        LocalPlatformAuthenticator provides platformAuthenticator,
        LocalPlatformAvailableAuthenticationOption provides platformPlatformAvailableAuthenticationOption,
        content = content,
    )
}

val LocalPlatformAuthenticator: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}

val LocalPlatformAvailableAuthenticationOption: ProvidableCompositionLocal<PlatformAvailableAuthenticationOption> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}