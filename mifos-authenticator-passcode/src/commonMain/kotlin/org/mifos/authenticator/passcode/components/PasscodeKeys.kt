package org.mifos.authenticator.passcode.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.passcode.theme.passcodeKeyButtonStyle



@Composable
fun PasscodeKeys(
    enterKey: (String) -> Unit,
    deleteKey: () -> Unit,
    deleteAllKeys: () -> Unit,
    togglePasscodeVisibility: () -> Unit,
    passcodeVisible: Boolean,
    config: PasscodeKeyConfig,
    modifier: Modifier = Modifier,
) {
    val onEnterKeyClick = { keyTitle: String ->
        enterKey(keyTitle)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "1",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "2",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "3",
                onClick = onEnterKeyClick,
                config = config
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "4",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "5",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "6",
                onClick = onEnterKeyClick,
                config = config
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "7",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "8",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "9",
                onClick = onEnterKeyClick,
                config = config
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {

            PasscodeKey(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(weight = 1.0F),
                keyIcon =
                    if (passcodeVisible) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff,
                keyIconContentDescription = "Toggle passcode visibility",
                onClick = {
                    togglePasscodeVisibility.invoke()
                },
                config = config
            )

            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyTitle = "0",
                onClick = onEnterKeyClick,
                config = config
            )
            PasscodeKey(
                modifier = Modifier.weight(weight = 1.0F),
                keyIcon = Icons.Filled.Backspace,
                keyIconContentDescription = "Delete Passcode Key Button",
                onClick = {
                    deleteKey()
                },
                onLongClick = {
                    deleteAllKeys()
                },
                config = config
            )
        }
    }
}

data class PasscodeKeyConfig(
    val keyColor: Color,
    val textStyle: TextStyle
)

@Composable
fun passcodeKeyConfig(
    keyColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = passcodeKeyButtonStyle().copy(color = keyColor)
) = PasscodeKeyConfig(
    keyColor,
    textStyle
)

@Composable
fun PasscodeKey(
    config: PasscodeKeyConfig,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyTitle: String = "",
    keyIcon: ImageVector? = null,
    keyIconContentDescription: String = "",
    onClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        CombinedClickableIconButton(
            modifier = Modifier
                .padding(all = 4.dp),
            onClick = {
                onClick?.invoke(keyTitle)
            },
            onLongClick = {
                onLongClick?.invoke()
            },
            enabled = enabled
        ) {
            if (keyIcon == null) {
                Text(
                    text = keyTitle,
                    style = config.textStyle
                )
            } else {
                Icon(
                    imageVector = keyIcon,
                    contentDescription = keyIconContentDescription,
                    tint = config.keyColor
                )
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
    rippleRadius: Dp = 36.dp,
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