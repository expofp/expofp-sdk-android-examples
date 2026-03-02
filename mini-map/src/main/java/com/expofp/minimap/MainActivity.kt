package com.expofp.minimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.expofp.minimap.data.PlanManager
import com.expofp.minimap.navigation.AppNavigation
import com.expofp.minimap.ui.theme.MiniMapTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var planManager: PlanManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start plan preloading early so it downloads while the UI is being set up.
        // The SDK must be initialized before this (see MiniMapApplication).
        planManager.preloadPlan()

        setContent {
            MiniMapTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dispose only when the activity is finishing (not on config changes).
        // Preloaded plans require explicit cleanup — the SDK won't do it automatically.
        if (isFinishing) {
            planManager.dispose()
        }
    }
}