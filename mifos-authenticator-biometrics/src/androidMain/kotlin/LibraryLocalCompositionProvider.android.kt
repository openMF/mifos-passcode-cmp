package com.mifos.passcode

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.mifos.passcode.platformAuthenticator.PlatformAvailableAuthenticationOption
import com.mifos.passcode.platformAuthenticator.PlatformAuthenticationProvider

@Composable
actual fun LibraryLocalCompositionProvider(
    content: @Composable (() -> Unit)
) {
    val activity = requireNotNull(LocalActivity.current) as FragmentActivity
    val contextLocal = LocalContext.current
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides activity,
        LibraryLocalContextProvider provides contextLocal,
        LibraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(activity),
        LibraryPlatformAvailableAuthenticationOption provides PlatformAvailableAuthenticationOption(contextLocal),
    ) {
        content()
    }
}