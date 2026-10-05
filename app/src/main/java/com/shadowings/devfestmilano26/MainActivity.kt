package com.shadowings.devfestmilano26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shadowings.devfestmilano26.core.navigation.AppNavHost
import com.shadowings.devfestmilano26.ui.theme.DevFestMilano26Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevFestMilano26Theme {
                AppNavHost()
            }
        }
    }
}
