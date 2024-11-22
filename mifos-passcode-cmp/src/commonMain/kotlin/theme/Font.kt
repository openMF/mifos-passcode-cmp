package com.mifos.passcode.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.mifos.mifos_passcode_cmp.generated.resources.Lato_Black
import com.mifos.mifos_passcode_cmp.generated.resources.Lato_Bold
import com.mifos.mifos_passcode_cmp.generated.resources.Lato_Regular
import com.mifos.mifos_passcode_cmp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun LatoFonts() = FontFamily(
    Font(
        resource = Res.font.Lato_Bold,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    ),
    Font(
        resource = Res.font.Lato_Regular,
        weight = FontWeight.Bold,
        style = FontStyle.Normal
    ),
    Font(
        resource = Res.font.Lato_Black,
        weight = FontWeight.Black,
        style = FontStyle.Normal
    )
)