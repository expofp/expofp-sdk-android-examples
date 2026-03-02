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
            // The SDK returns the same View instance for the same presenter.
            // A View can only have one parent, so detach it from any previous parent first.
            (planView.parent as? ViewGroup)?.removeView(planView)
            FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(planView)
            }
        },
        // Detach the SDK view when this composable leaves composition,
        // so it can be re-attached elsewhere if needed.
        onRelease = { it.removeAllViews() },
        modifier = modifier
    )
}
