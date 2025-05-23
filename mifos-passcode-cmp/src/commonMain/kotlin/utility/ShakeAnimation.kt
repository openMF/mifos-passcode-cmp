package com.mifos.passcode.utility

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ShakeAnimation {

    fun CoroutineScope.performShakeAnimation(xShake: Animatable<Float, *>) {
        launch {
            xShake.animateTo(
                targetValue = 0f, // This resets the position after the shake
                animationSpec = keyframes {
                    durationMillis = 280 // Total animation duration
                    0f at 0 using LinearOutSlowInEasing // Start position
                    20f at 80 using LinearOutSlowInEasing // Move right
                    -20f at 120 using LinearOutSlowInEasing // Move left
                    10f at 160 using LinearOutSlowInEasing // Move right
                    -10f at 200 using LinearOutSlowInEasing // Move left
                    5f at 240 using LinearOutSlowInEasing // Move right
                    0f at 280 // End at the original position
                }
            )
        }
    }
}