package com.expofp.minimap.ui.components

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
        // getView() returns a self-sizing container and detaches from any previous parent,
        // so it can be hosted directly. alpha = 1f undoes the pre-warm alpha = 0f set elsewhere.
        factory = { presenter.getView().apply { alpha = 1f } },
        modifier = modifier
    )
}
