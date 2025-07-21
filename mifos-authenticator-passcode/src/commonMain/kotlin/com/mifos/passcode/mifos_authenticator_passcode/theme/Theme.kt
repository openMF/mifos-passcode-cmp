package com.mifos.passcode.mifos_authenticator_passcode.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mifos.passcode.core.designsystem.theme.Typography
import com.mifos.passcode.core.designsystem.theme.blueTint

private val DarkColorPalette = darkColorScheme(
    primary = blueTint,
    onPrimary = Color.White,
    secondary = Color.Black.copy(alpha = 0.2f),
    background = Color.Black
)
private val LightColorPalette = lightColorScheme(
    primary = blueTint,
    onPrimary = Color.White,
    secondary = Color.White,
    background = Color.White
)

@Composable
fun MifosPasscodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}