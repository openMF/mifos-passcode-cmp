package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator


val LibraryLocalAndroidActivity = compositionLocalOf { Any() }
val LibraryLocalContextProvider = compositionLocalOf { Any() }
@Composable
fun LocalCompositionProvider(
    activity: Any = LibraryLocalAndroidActivity.current,
    context: Any = LibraryLocalContextProvider.current,
    platformAuthenticationProvider: PlatformAuthenticationProvider = PlatformAuthenticationProvider(activity = activity),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides activity,
        LibraryLocalContextProvider provides context,
        LibraryLocalPlatformAuthenticatorProvider provides platformAuthenticationProvider,
        content = content,
    )
}


val LibraryLocalPlatformAuthenticatorProvider = staticCompositionLocalOf<PlatformAuthenticationProvider> {
    error("No PlatformAuthenticationProvider provided")
}