package org.mifos.authenticator.passcode.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.passcode.theme.passcodeKeyButtonStyle
import org.mifos.authenticator.passcode.theme.blueTint

@Composable
fun PasscodeKeys(
    modifier: Modifier = Modifier,
    enterKey: (String) -> Unit,
    deleteKey: () -> Unit,
    deleteAllKeys: () -> Unit,
    togglePasscodeVisibility: () -> Unit,
    passcodeVisible: Boolean,
    shouldJumbleKeys: Boolean = false,
    keyTextStyle: TextStyle = passcodeKeyButtonStyle(),
    keyColor: Color = blueTint,
    keyShape: Shape = CircleShape,
    keyElevation: CardElevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    keyContainerColor: Color = Color.White,
    keySize: Dp = 60.dp
) {
    val onEnterKeyClick = { keyTitle: String ->
        enterKey(keyTitle)
    }

    val keys = rememberSaveable(shouldJumbleKeys) {
        val baseKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        if (shouldJumbleKeys) baseKeys.shuffled() else baseKeys
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val keyModifier = Modifier.weight(weight = 1.0F)
            .padding(2.dp)

        for (i in 0 until 3) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 3) {
                    val keyTitle = keys[i * 3 + j]
                    PasscodeKey(
                        modifier = keyModifier,
                        keyTitle = keyTitle,
                        onClick = onEnterKeyClick,
                        keyTextStyle = keyTextStyle,
                        keyColor = keyColor,
                        shape = keyShape,
                        elevation = keyElevation,
                        containerColor = keyContainerColor,
                        size = keySize
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {

            PasscodeKey(
                modifier = keyModifier,
                keyIcon = if (passcodeVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                keyIconContentDescription = "Toggle passcode visibility",
                onClick = {
                    togglePasscodeVisibility.invoke()
                },
                keyColor = keyColor,
                shape = keyShape,
                elevation = keyElevation,
                containerColor = keyContainerColor,
                size = keySize
            )

            PasscodeKey(
                modifier = keyModifier,
                keyTitle = keys[9],
                onClick = onEnterKeyClick,
                keyTextStyle = keyTextStyle,
                keyColor = keyColor,
                shape = keyShape,
                elevation = keyElevation,
                containerColor = keyContainerColor,
                size = keySize
            )
            PasscodeKey(
                modifier = keyModifier,
                keyIcon = Icons.Filled.Backspace,
                keyIconContentDescription = "Delete Passcode Key Button",
                onClick = {
                    deleteKey()
                },
                onLongClick = {
                    deleteAllKeys()
                },
                keyColor = keyColor,
                shape = keyShape,
                elevation = keyElevation,
                containerColor = keyContainerColor,
                size = keySize
            )
        }
    }
}

@Composable
fun PasscodeKey(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyTitle: String = "",
    keyIcon: ImageVector? = null,
    keyIconContentDescription: String = "",
    onClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    keyTextStyle: TextStyle = passcodeKeyButtonStyle(),
    keyColor: Color = blueTint,
    shape: Shape = CircleShape,
    elevation: CardElevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    containerColor: Color = Color.White,
    size: Dp = 60.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.size(size),
            shape = shape,
            elevation = elevation,
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        ) {
            CombinedClickableIconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    onClick?.invoke(keyTitle)
                },
                onLongClick = {
                    onLongClick?.invoke()
                },
                enabled = enabled,
                size = size
            ) {
                if (keyIcon == null) {
                    Text(
                        text = keyTitle,
                        style = keyTextStyle.copy(color = keyColor)
                    )
                } else {
                    Icon(
                        imageVector = keyIcon,
                        contentDescription = keyIconContentDescription,
                        tint = keyColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CombinedClickableIconButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .size(size = size)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = enabled,
                role = Role.Button,
                indication = ripple(),
                interactionSource = interactionSource,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val contentAlpha =
            if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0f)
        CompositionLocalProvider(LocalContentColor provides contentAlpha, content = content)
    }
}
