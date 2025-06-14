package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider


val LibraryLocalAndroidActivity: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
val LibraryLocalContextProvider:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

@Composable
expect fun LibraryLocalCompositionProvider(
    content: @Composable () -> Unit,
)


val LibraryLocalPlatformAuthenticationProvider: ProvidableCompositionLocal<PlatformAuthenticationProvider> = compositionLocalOf {
    error("CompositionLocal of PlatformAuthenticationProvider not provided")
}

val LibraryPlatformAvailableAuthenticationOption: ProvidableCompositionLocal<PlatformAvailableAuthenticationOption> = compositionLocalOf {
    error("CompositionLocal of PlatformAvailableAuthenticationOption not provided")
}