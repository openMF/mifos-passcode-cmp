package com.mifos.passcode.sample.platformAuthentication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticatorStatus
import com.mifos.passcode.sample.Platform
import com.mifos.passcode.sample.getPlatform
import com.mifos.passcode.ui.theme.blueTint
import mifos_passcode_cmp.sample.composeapp.generated.resources.Res
import mifos_passcode_cmp.sample.composeapp.generated.resources.eye_scanner
import mifos_passcode_cmp.sample.composeapp.generated.resources.face_scan
import mifos_passcode_cmp.sample.composeapp.generated.resources.fingerprint
import mifos_passcode_cmp.sample.composeapp.generated.resources.keypad
import org.jetbrains.compose.resources.painterResource

@Composable
fun SystemAuthenticatorButton(
    onClick: () -> Unit,
    platformAuthOptions: List<PlatformAuthOptions> = listOf(PlatformAuthOptions.UserCredential),
    authenticatorStatus: Set<PlatformAuthenticatorStatus>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.Center
    ){
        when(getPlatform()){
            Platform.ANDROID ->{
                if(authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)){
                    if(
                        platformAuthOptions.contains(PlatformAuthOptions.Iris) ||
                        (platformAuthOptions.contains(PlatformAuthOptions.FaceId) &&
                                platformAuthOptions.contains(PlatformAuthOptions.Fingerprint))
                    ){
                        ClickableTextButton(
                            onClick = onClick,
                            text = "Use Biometrics"
                        )
                    }else if(platformAuthOptions.contains(PlatformAuthOptions.Fingerprint)){
                        Image(
                            painter = painterResource(Res.drawable.fingerprint),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable{onClick()}
                        )
                    }else if(platformAuthOptions.contains(PlatformAuthOptions.FaceId)){
                        Image(
                            painter = painterResource(Res.drawable.face_scan),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable{onClick()}
                        )
                    }else if(platformAuthOptions.contains(PlatformAuthOptions.Iris)){
                        Image(
                            painter = painterResource(Res.drawable.eye_scanner),
                            contentDescription = "Fingerprint icon",
                            modifier = Modifier.size(50.dp)
                                .clickable{onClick()}
                        )
                    }
                }else if(authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)){
                    Image(
                        painter = painterResource(Res.drawable.keypad),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(50.dp)
                            .clickable{onClick()}
                    )
                }else {
                    Text("Set up Authentication Option")
                }
            }
            Platform.IOS -> {
                if(authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)){
                    Image(
                        painter = painterResource(Res.drawable.face_scan),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(40.dp)
                            .clickable{onClick()}
                    )
                } else if(authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)){
                    Image(
                        painter = painterResource(Res.drawable.keypad),
                        contentDescription = "Fingerprint icon",
                        modifier = Modifier.size(40.dp)
                            .clickable{onClick()}
                    )
                }else {
                    Text("Set up Authentication Option")
                }
            }
            Platform.JVM -> {
                if(
                    authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)  ||
                    authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
                ) {
                    ClickableTextButton(
                        onClick = onClick,
                        text = "Authenticate using Windows Hello"
                    )
                } else{
                    Text("Unsupported platform")
                }
            }
            Platform.JS -> {
                Text("Unsupported platform")
            }
            Platform.WASMJS ->{
                Text("Unsupported platform")
            }
        }
    }
}


@Composable
fun ClickableTextButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String
){
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.height(50.dp)
                .width(250.dp)
                .clip(
                    RoundedCornerShape(30.dp)
                )
                .background(color = blueTint),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text,
                    color = Color.White
                )
            }
        }
    }
}

