

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.mifos.passcode.sample.navigation.PasscodeNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        PasscodeNavigation()
    }
}