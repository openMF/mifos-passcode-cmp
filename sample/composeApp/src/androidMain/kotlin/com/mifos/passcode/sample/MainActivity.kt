package com.mifos.passcode.sample

import App
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.mifos.passcode.LocalCompositionProvider
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * I am using FragmentActivity here because when MainActivity (when AppCompatActivity or ComponentActivity) is attempted to cast into FragmentActivity
 * the app is crashing.
 * Exception: java.lang.ClassCastException: com.mifos.passcode.sample.MainActivity cannot be cast to androidx.fragment.app.FragmentActivity
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalCompositionProvider(this) {
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