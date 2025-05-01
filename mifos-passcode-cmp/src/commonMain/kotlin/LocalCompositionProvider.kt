package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel

val LocalAndroidActivity = compositionLocalOf { Any() }

@Composable
fun LocalCompositionProvider(
    activity: Any,
    platformAuthenticator: PlatformAuthenticator = PlatformAuthenticator(activity = activity),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAndroidActivity provides activity,
        LocalPlatformAuthenticator provides platformAuthenticator,
        content = content,
    )
}

val LocalPlatformAuthenticator: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
    error("CompositionLocal PlatformAuthenticator not present")
}

val LocalDeviceAuthViewModel: ProvidableCompositionLocal<DeviceAuthenticatorViewModel> = compositionLocalOf {
    error("CompositionLocal DeviceAuthenticatorViewModel not present")
}