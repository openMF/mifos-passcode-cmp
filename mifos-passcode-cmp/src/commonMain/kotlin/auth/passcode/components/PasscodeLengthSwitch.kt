package com.mifos.passcode.auth.passcode.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mifos.passcode.ui.theme.blueTint
import com.mifos.passcode.utility.PasscodeLength

@Composable
fun PasscodeLengthSwitch(
    modifier: Modifier = Modifier,
    passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    switchColor: Color = blueTint,
    enabledTextColor: Color = Color.Black,
    disabledTextColor: Color = Color.White,
    onSelectFourDigit: () -> Unit = {},
    onSelectSixDigit: () -> Unit = {},
){
    val border= BorderStroke(
        2.dp,
        color = switchColor
    )
    Box (
        modifier = modifier
            .height(35.dp)
            .width(150.dp)
            .clip(RoundedCornerShape(40.dp))
            .border(border.width, border.brush, RoundedCornerShape(100))
    ) {

        var passcodeLength by remember {
            mutableStateOf(passcodeLength)
        }


        Button(
            {
                passcodeLength = PasscodeLength.FOUR_DIGIT
                onSelectFourDigit()
            },
            modifier = Modifier
                .fillMaxWidth(.5f)
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        40.dp
                    )
                )
                .align(Alignment.CenterStart),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = enabledTextColor,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            enabled = passcodeLength == PasscodeLength.SIX_DIGIT,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("4 digits")
        }

        AnimatedContent(
            targetState = passcodeLength,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color.Transparent)
                .clip(RoundedCornerShape(40.dp)),
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = {
                        if(passcodeLength.length==4) it else -it
                    }
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = {
                        if(passcodeLength.length==4) -it else it
                    }
                )
            },
            content = { passcodeLength ->
                if(passcodeLength.length==6){
                    Box(
                        contentAlignment = Alignment.CenterEnd
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(.5f)
                                .clip(RoundedCornerShape(40.dp))
                                .border(border.width, border.brush, RoundedCornerShape(100))
                                .background(switchColor),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            Text("6 digits", color = disabledTextColor)
                        }
                    }
                }else{
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(.5f)
                                .clip(RoundedCornerShape(40.dp))
                                .border(border.width, border.brush, RoundedCornerShape(100))
                                .background(switchColor),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            Text("4 digits", color = disabledTextColor)
                        }
                    }
                }
            },

            label = ""
        )



        Button(
            {
                passcodeLength = PasscodeLength.SIX_DIGIT
                onSelectSixDigit()
            },
            modifier = Modifier
                .fillMaxWidth(.5f)
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        40.dp
                    )
                )
                .align(Alignment.CenterEnd),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = enabledTextColor,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            contentPadding = PaddingValues(0.dp),
            enabled = passcodeLength == PasscodeLength.FOUR_DIGIT,
        ) {
            Text("6 digits")
        }

    }
}