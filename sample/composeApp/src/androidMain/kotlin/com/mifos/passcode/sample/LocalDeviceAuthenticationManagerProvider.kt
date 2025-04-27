package com.mifos.passcode.sample

import AndroidAuthenticator
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mifos.passcode.deviceAuth.domain.PlatformAuthenticator


//@Composable
//fun LocalDeviceAuthenticationManagerProvider(
//    activity: Activity = requireNotNull(LocalActivity.current),
//    platformAuthenticator: PlatformAuthenticator = AndroidAuthenticator(activity = activity),
//    content: @Composable () -> Unit,
//) {
//    CompositionLocalProvider(
//        LocalDeviceAuthenticationManager provides platformAuthenticator,
//        content = content,
//    )
//}
//
//val LocalDeviceAuthenticationManager: ProvidableCompositionLocal<PlatformAuthenticator> = compositionLocalOf {
//    error("CompositionLocal BiometricsManager not present")
//}