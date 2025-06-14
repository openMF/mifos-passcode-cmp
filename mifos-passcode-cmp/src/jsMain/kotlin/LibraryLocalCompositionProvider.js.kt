package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider


@Composable
actual fun LibraryLocalCompositionProvider(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides null,
        LibraryLocalContextProvider provides null,
        LibraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(),
        LibraryPlatformAvailableAuthenticationOption provides PlatformAvailableAuthenticationOption(),
    ){
        content()
    }
}