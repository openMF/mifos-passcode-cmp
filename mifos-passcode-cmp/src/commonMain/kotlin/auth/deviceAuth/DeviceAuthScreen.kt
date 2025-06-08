package com.mifos.passcode.auth.deviceAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mifos.passcode.LibraryLocalContextProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.components.ClickableTextButton
import com.mifos.passcode.auth.deviceAuth.components.SystemAuthenticatorButton
import com.mifos.passcode.auth.passcode.components.MifosIcon
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAuthScreen(
    platformAuthenticatorStatus: PlatformAuthenticatorStatus,
    onClickAuthentication: () -> Unit,
    onLogout: () -> Unit = {},
) {

    val scope = rememberCoroutineScope()

    val platformAvailableAuthenticationOption: PlatformAvailableAuthenticationOption? =
        PlatformAvailableAuthenticationOption(
            LibraryLocalContextProvider.current
        )

    Scaffold(
        topBar = {
            TopAppBar(
                {Text(stringResource(Res.string.app_name))},
                actions = {
                    Button(
                        onClick = onLogout
                    ){Text("Log out")}
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

//            ClickableTextButton(
//                onClick = onClickAuthentication,
//                true,
//                "Authentication",
//            )
            SystemAuthenticatorButton(
                onClick = {
                    onClickAuthentication()
                },
                platformAuthOptions =
                    platformAvailableAuthenticationOption?.getAuthOption()?: listOf(PlatformAuthOptions.UserCredential),
                authenticatorStatus = platformAuthenticatorStatus
            )
        }
    }

}

