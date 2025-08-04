package org.mifos.authenticator.passcode.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.core.designsystem.theme.blueTint
import org.mifos.authenticator.passcode.utility.Constants
import org.mifos.authenticator.passcode.utility.Step

data class PasscodeStepIndicatorConfig(
    val activeStepColor: Color,
    val inactiveStepColor: Color,
    val shape: Shape
)

@Composable
fun passcodeStepIndicatorConfig(
    activeStepColor: Color = MaterialTheme.colorScheme.primary,
    inactiveStepColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape: Shape = MaterialTheme.shapes.medium
) = PasscodeStepIndicatorConfig(
    activeStepColor,
    inactiveStepColor,
    shape
)

@Composable
fun PasscodeStepIndicator(
    modifier: Modifier = Modifier,
    activeStep: Step,
    config: PasscodeStepIndicatorConfig = passcodeStepIndicatorConfig()
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        repeat(Constants.STEPS_COUNT) { step ->
            val isActiveStep = step <= activeStep.index
            val stepColor =
                animateColorAsState(
                    if (isActiveStep) config.activeStepColor
                    else config.inactiveStepColor,
                    label = ""
                )

            Box(
                modifier = Modifier
                    .size(
                        width = 80.dp,
                        height = 4.dp
                    )
                    .background(
                        color = stepColor.value,
                        shape = config.shape
                    )
            )
        }
    }
}