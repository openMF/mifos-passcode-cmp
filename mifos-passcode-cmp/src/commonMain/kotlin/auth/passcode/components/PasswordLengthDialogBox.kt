package com.mifos.passcode.auth.passcode.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mifos.passcode.auth.passcode.components.PasscodeLength.FOUR_DIGIT
import com.mifos.passcode.auth.passcode.components.PasscodeLength.SIX_DIGIT
import com.mifos.passcode.ui.theme.blueTint

@Composable
fun SelectPasscodeLengthDialogBox(
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    currentPasscodeLength: Int
){

    var selected by remember {
        mutableStateOf(currentPasscodeLength)
    }

    Dialog(
        onDismissRequest = onDismiss
    ){
        Column(
            modifier = Modifier.clip(
                RoundedCornerShape(25.dp)
            )
            .fillMaxWidth(0.9f)
            .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                "Select Passcode Length",
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontSize = 16.sp
                ),
                color = blueTint
            )
            Row(
                Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "4 digits",
                    fontSize = 16.sp
                )
                RadioButton(
                    selected = selected == FOUR_DIGIT.length,
                    onClick = {
                        onSelected(FOUR_DIGIT.length)
                        onDismiss()
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = blueTint
                    )
                )
            }

            HorizontalDivider()

            Row(
                Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "6 digits",
                    fontSize = 16.sp
                )
                RadioButton(
                    selected = selected == SIX_DIGIT.length,
                    onClick = {
                        onSelected(SIX_DIGIT.length)
                        onDismiss()
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = blueTint
                    )
                )
            }

            HorizontalDivider()

            TextButton(
                onClick = onDismiss
            ){
                Text(
                    "Cancel",
                    color = blueTint
                )
            }

        }

    }
}


private enum class PasscodeLength(val length: Int){
    FOUR_DIGIT(4),
    SIX_DIGIT(6)
}
