package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator

val LibraryLocalAndroidActivity = compositionLocalOf { Any() }
val LibraryLocalContextProvider = compositionLocalOf { Any() }
@Composable
fun LocalCompositionProvider(
    activity: Any = LibraryLocalAndroidActivity.current,
    context: Any = LibraryLocalContextProvider.current,
    platformAuthenticator: PlatformAuthenticator = PlatformAuthenticator(activity = activity),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides activity,
        LibraryLocalContextProvider provides context,
        LocalPlatformAuthenticator provides platformAuthenticator,
        content = content,
    )
}

val LocalPlatformAuthenticator: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}