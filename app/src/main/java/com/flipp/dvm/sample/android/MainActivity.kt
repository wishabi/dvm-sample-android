package com.flipp.dvm.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flipp.dvm.sample.android.ui.screens.DvmSampleApp
import com.flipp.dvm.sample.android.ui.theme.DvmSdkAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DvmSdkAndroidTheme {
                DvmSampleApp()
            }
        }
    }
}
