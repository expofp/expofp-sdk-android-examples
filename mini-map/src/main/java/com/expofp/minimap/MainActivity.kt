package com.expofp.minimap

import android.os.Bundle
import android.view.ViewGroup
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
        preAttachWebViews()

        setContent {
            MiniMapTheme {
                AppNavigation()
            }
        }
    }

    private fun preAttachWebViews() {
        val container = findViewById<ViewGroup>(android.R.id.content)
        listOf(planManager.miniMapPresenter, planManager.fullMapPresenter).forEach { presenter ->
            presenter?.getView()?.let { view ->
                view.alpha = 0f
                container.addView(view, 0)
            }
        }
    }
}