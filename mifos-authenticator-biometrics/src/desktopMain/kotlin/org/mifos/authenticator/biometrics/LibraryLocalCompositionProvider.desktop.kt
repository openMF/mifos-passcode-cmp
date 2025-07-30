package org.mifos.authenticator.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider


@Composable
actual fun LibraryLocalCompositionProvider(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides null,
        LibraryLocalContextProvider provides null,
        LibraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(),
        LibraryPlatformAvailableAuthenticationOption provides _root_ide_package_.org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption(),
    ){
        content()
    }
}