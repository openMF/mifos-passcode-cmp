package com.mifos.passcode.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Using Fragment Activity or AppCompatActivity is must.
 * ComponentActivity does not provide support for fragment and fragment activity.
 * Therefore it cannot be casted to fragment activity.
 */

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalCompositionProvider(
                activity = this,
                context = this
            ) {
                App()
            }
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}