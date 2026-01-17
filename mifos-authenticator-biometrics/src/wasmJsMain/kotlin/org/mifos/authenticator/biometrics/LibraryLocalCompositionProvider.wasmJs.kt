package org.mifos.authenticator.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider


@Composable
actual fun LibraryLocalCompositionProvider(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        libraryLocalAndroidActivity provides null,
        libraryLocalContextProvider provides null,
        libraryLocalPlatformAuthenticationProvider provides PlatformAuthenticationProvider(),
        libraryPlatformAvailableAuthenticationOption provides _root_ide_package_.org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption(),
    ){
        content()
    }
}