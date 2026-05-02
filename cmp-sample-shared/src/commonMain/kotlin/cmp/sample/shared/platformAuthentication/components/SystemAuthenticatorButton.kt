/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.platformAuthentication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cmp.sample.shared.theme.blueTint
import mifos_authenticator.cmp_sample_shared.generated.resources.Res
import mifos_authenticator.cmp_sample_shared.generated.resources.authenticate_using_windows_hello
import mifos_authenticator.cmp_sample_shared.generated.resources.eye_scanner
import mifos_authenticator.cmp_sample_shared.generated.resources.face_scan
import mifos_authenticator.cmp_sample_shared.generated.resources.fingerprint
import mifos_authenticator.cmp_sample_shared.generated.resources.keypad
import mifos_authenticator.cmp_sample_shared.generated.resources.setup_authentication_option
import mifos_authenticator.cmp_sample_shared.generated.resources.unsupported_platform
import mifos_authenticator.cmp_sample_shared.generated.resources.use_biometrics
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.biometrics.Platform
import org.mifos.authenticator.biometrics.getPlatform
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthOptions
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus

@Composable
fun SystemAuthenticatorButton(
    onClick: () -> Unit,
    platformAuthOptions: List<PlatformAuthOptions> = listOf(PlatformAuthOptions.UserCredential),
    authenticatorStatus: Set<PlatformAuthenticatorStatus>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        when (getPlatform()) {
            Platform.ANDROID -> {
                if (authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)) {
                    if (
                        platformAuthOptions.contains(PlatformAuthOptions.Iris) ||
                        (
                            platformAuthOptions.contains(PlatformAuthOptions.FaceId) &&
                                platformAuthOptions.contains(PlatformAuthOptions.Fingerprint)
                            )
                    ) {
                        AuthenticateButton(
                            onClick = onClick,
                            text = stringResource(Res.string.use_biometrics),
                        )
                    } else if (platformAuthOptions.contains(PlatformAuthOptions.Fingerprint)) {
                        Image(
                            painter = painterResource(Res.drawable.fingerprint),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable { onClick() },
                        )
                    } else if (platformAuthOptions.contains(PlatformAuthOptions.FaceId)) {
                        Image(
                            painter = painterResource(Res.drawable.face_scan),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable { onClick() },
                        )
                    } else if (platformAuthOptions.contains(PlatformAuthOptions.Iris)) {
                        Image(
                            painter = painterResource(Res.drawable.eye_scanner),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable { onClick() },
                        )
                    } else if (platformAuthOptions.contains(PlatformAuthOptions.UserCredential)) {
                        Image(
                            painter = painterResource(Res.drawable.keypad),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable { onClick() },
                        )
                    }
                } else if (authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)) {
                    Image(
                        painter = painterResource(Res.drawable.keypad),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(50.dp)
                            .clickable { onClick() },
                    )
                } else {
                    Text(stringResource(Res.string.setup_authentication_option))
                }
            }
            Platform.IOS -> {
                if (authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)) {
                    Image(
                        painter = painterResource(Res.drawable.face_scan),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(40.dp)
                            .clickable { onClick() },
                    )
                } else if (authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)) {
                    Image(
                        painter = painterResource(Res.drawable.keypad),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(40.dp)
                            .clickable { onClick() },
                    )
                } else {
                    Text(stringResource(Res.string.setup_authentication_option))
                }
            }
            Platform.JVM -> {
                if (
                    authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET) ||
                    authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
                ) {
                    AuthenticateButton(
                        onClick = onClick,
                        text = stringResource(Res.string.authenticate_using_windows_hello),
                    )
                } else {
                    Text(stringResource(Res.string.unsupported_platform))
                }
            }
            Platform.JS -> {
                Text(stringResource(Res.string.unsupported_platform))
            }
            Platform.WASMJS -> {
                Text(stringResource(Res.string.unsupported_platform))
            }
        }
    }
}

@Composable
fun AuthenticateButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(50.dp)
            .width(250.dp)
            .clip(
                RoundedCornerShape(30.dp),
            )
            .background(color = blueTint),
    ) {
        Text(
            text,
            color = Color.White,
        )
    }
}
