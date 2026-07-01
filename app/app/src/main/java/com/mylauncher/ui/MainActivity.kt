package com.mylauncher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylauncher.data.model.toComposeColor
import com.mylauncher.ui.navigation.LauncherNavHost
import com.mylauncher.ui.theme.MyLauncherTheme
import com.mylauncher.ui.viewmodel.LauncherViewModel
import com.mylauncher.widget.LauncherWidgetHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var widgetHost: LauncherWidgetHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Launchers should not exit on back press — intercept via OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this) { /* no-op */ }

        setContent {
            val viewModel: LauncherViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val accentColor = uiState.preferences.accentColorArgb.toComposeColor()

            MyLauncherTheme(accentColor = accentColor) {
                LauncherNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }
}
