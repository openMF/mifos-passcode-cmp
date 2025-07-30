package org.mifos.authenticator.passcode.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import org.mifos.authenticator.core.designsystem.theme.blueTint
import org.mifos.authenticator.passcode.utility.PasscodeLength

@Composable
fun PasscodeLengthSwitch(
    modifier: Modifier = Modifier,
    passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    tabColor: Color = blueTint,
    enabledSwitchColor: Color = Color.LightGray.copy(alpha = .7f),
    enabledTextColor: Color = Color.Black,
    disabledTextColor: Color = Color.White, // Text color on the sliding tab
    onSelectFourDigit: () -> Unit = {},
    onSelectSixDigit: () -> Unit = {},
){

    var selectedPasscodeLength by remember {
        mutableStateOf(passcodeLength)
    }

    Box (
        modifier = modifier
            .height(35.dp)
            .width(150.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(enabledSwitchColor)
    ) {


        Button(
            {
                selectedPasscodeLength = PasscodeLength.FOUR_DIGIT
                onSelectFourDigit()
            },
            modifier = Modifier
                .fillMaxWidth(.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(40.dp))
                .align(Alignment.CenterStart),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = enabledTextColor,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            enabled = selectedPasscodeLength == PasscodeLength.SIX_DIGIT,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("4 digits")
        }

        AnimatedContent(
            targetState = selectedPasscodeLength,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color.Transparent)
                .clip(RoundedCornerShape(40.dp)),
            transitionSpec = {
                val isGoingRight = targetState.length > initialState.length
                slideInHorizontally(
                    spring(
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetX = {
                        if(isGoingRight) it else -it
                    }
                ) togetherWith slideOutHorizontally(
                    spring(
                        stiffness = Spring.StiffnessLow
                    ),
                    targetOffsetX = {
                        if(isGoingRight) -it else it
                    }
                )
            },
            content = { currentPasscodeLength ->
                SlidingTab(
                    label =
                        if (currentPasscodeLength == PasscodeLength.SIX_DIGIT) "6 digits"
                        else "4 digits",
                    alignment =
                        if (currentPasscodeLength == PasscodeLength.SIX_DIGIT) Alignment.CenterEnd
                        else Alignment.CenterStart,
                    color = tabColor,
                    labelColor = disabledTextColor
                )
            },

            label = ""
        )



        Button(
            {
                selectedPasscodeLength = PasscodeLength.SIX_DIGIT
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
            enabled = selectedPasscodeLength == PasscodeLength.FOUR_DIGIT,
        ) {
            Text("6 digits")
        }

    }
}

@Composable
private fun SlidingTab(
    label: String ,
    color: Color,
    labelColor: Color,
    alignment: Alignment
){
    Box(
        contentAlignment = alignment
    ){
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(.5f)
                .clip(RoundedCornerShape(40.dp))
                .background(color),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(label, color = labelColor)
        }
    }
}