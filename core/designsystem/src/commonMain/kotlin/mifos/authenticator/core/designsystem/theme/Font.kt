package mifos.authenticator.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import mifos_authenticator.core.designsystem.generated.resources.Lato_Black
import mifos_authenticator.core.designsystem.generated.resources.Lato_Bold
import mifos_authenticator.core.designsystem.generated.resources.Lato_Regular
import mifos_authenticator.core.designsystem.generated.resources.Res
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