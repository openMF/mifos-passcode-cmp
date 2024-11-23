package com.mifos.passcode.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.mifos_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun MifosIcon(modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(180.dp),
            painter = painterResource(resource= Res.drawable.mifos_logo),
            contentDescription = null
        )
    }
}