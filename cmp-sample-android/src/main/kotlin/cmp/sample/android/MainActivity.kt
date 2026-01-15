package cmp.sample.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import cmp.sample.shared.App

/**
 * Using Fragment Activity or AppCompatActivity is must.
 * ComponentActivity does not provide support for fragment and fragment activity.
 * Therefore, it cannot be cast to fragment activity.
 */

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}