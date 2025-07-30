package mifos.authenticator.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import mifos.authenticator.biometrics.platformAuthenticator.PlatformAvailableAuthenticationOption
import mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider


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