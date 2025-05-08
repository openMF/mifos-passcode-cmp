package com.mifos.passcode.auth.passcode.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mifos.passcode.ui.theme.blueTint
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.enable_biometric_dialog_description
import io.github.openmf.mifos_passcode_cmp.generated.resources.enable_biometric_dialog_title
import io.github.openmf.mifos_passcode_cmp.generated.resources.no
import io.github.openmf.mifos_passcode_cmp.generated.resources.yes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemAuthSetupConfirmDialog(
    cancelSetup: () -> Unit,
    setSystemAuthentication: () -> Unit
) {

    val dialogProperties = DialogProperties()

    Dialog(
        onDismissRequest = {cancelSetup.invoke()},
        properties = dialogProperties
    ){
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(White)
                .padding(16.dp)
        ) {

            Column {

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(resource = Res.string.enable_biometric_dialog_title),
                    modifier = Modifier
                        .padding(8.dp),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(resource = Res.string.enable_biometric_dialog_description),
                    modifier = Modifier
                        .padding(8.dp),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    DialogButton(
                        onClick = { cancelSetup.invoke() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f),
                        text = stringResource(resource = Res.string.no)
                    )

                    DialogButton(
                        onClick = { setSystemAuthentication.invoke() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                        text = stringResource(resource = Res.string.yes)
                    )
                }
            }
        }
    }
}


@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = blueTint,
            contentColor = White,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = White
        )
    ) {
        Text(text = text)
    }
}