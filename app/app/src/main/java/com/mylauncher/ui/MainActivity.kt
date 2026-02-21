package com.mylauncher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylauncher.data.model.toComposeColor
import com.mylauncher.ui.navigation.LauncherNavHost
import com.mylauncher.ui.theme.MyLauncherTheme
import com.mylauncher.ui.viewmodel.LauncherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: LauncherViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val accentColor = uiState.preferences.accentColorArgb.toComposeColor()

            MyLauncherTheme(accentColor = accentColor) {
                LauncherNavHost(viewModel = viewModel)
            }
        }
    }

    /**
     * Override back press to do nothing — this is a launcher, so Home = this app.
     * On Android 13+ the system handles this differently.
     */
    @Suppress("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Launchers should not exit on back press
    }
}
