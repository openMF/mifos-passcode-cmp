package org.mifos.authenticator.passcode.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_logo
import org.jetbrains.compose.resources.painterResource


@Composable
fun MifosIcon(
    modifier: Modifier = Modifier,
    logoSize: Dp = 180.dp,
    logoPainter: Painter = painterResource(resource = Res.drawable.mifos_logo)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(logoSize),
            painter = logoPainter,
            contentDescription = null
        )
    }
}
