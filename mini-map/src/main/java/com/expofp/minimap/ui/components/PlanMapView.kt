package com.expofp.minimap.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.expofp.fplan.api.app.IExpoFpPlanPresenter

@Composable
fun PlanMapView(
    presenter: IExpoFpPlanPresenter,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            val planView = presenter.getView()
            (planView.parent as? ViewGroup)?.removeView(planView)
            planView.alpha = 1f
            FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(planView)
            }
        },
        onRelease = { it.removeAllViews() },
        modifier = modifier
    )
}
