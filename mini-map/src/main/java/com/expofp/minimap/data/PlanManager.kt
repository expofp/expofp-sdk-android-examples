package com.expofp.minimap.data

import com.expofp.fplan.api.app.ExpoFpPlan
import com.expofp.fplan.api.app.IExpoFpPlanPresenter
import com.expofp.fplan.api.app.model.ExpoFpError
import com.expofp.fplan.api.app.model.ExpoFpLinkType
import com.expofp.fplan.api.app.model.ExpoFpPlanParameter
import com.expofp.fplan.api.app.model.ExpoFpPlanStatus
import com.expofp.fplan.api.app.model.ExpoFpResult
import com.expofp.fplan.api.preloader.ExpoFpPreloadedPlanInfo
import com.expofp.fplan.jsInteraction.model.ExpoFpBooth
import com.expofp.fplan.jsInteraction.model.ExpoFpElementsVisibility
import com.expofp.fplan.jsInteraction.model.ExpoFpExhibitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanManager @Inject constructor() {

    private var planInfo: ExpoFpPreloadedPlanInfo? = null

    private val _presenter = MutableStateFlow<IExpoFpPlanPresenter?>(null)
    val presenter: StateFlow<IExpoFpPlanPresenter?> = _presenter.asStateFlow()

    // Starts asynchronous plan preloading. The plan downloads in the background;
    // observe getPlanStatusFlow() to track progress and know when it's ready.
    fun preloadPlan() {
        if (planInfo != null) return

        val params = listOf(
            ExpoFpPlanParameter.NoOverlay(true),
            ExpoFpPlanParameter.HideHeaderLogo(true)
        )
        planInfo = ExpoFpPlan.preloader.preloadPlan(
            planLink = ExpoFpLinkType.ExpoKey(PLAN_KEY),
            additionalParams = params
        )
    }

    // Must be called when the app is finishing to release the preloaded plan resources.
    // Preloaded plans are NOT automatically cleaned up by the SDK.
    fun dispose() {
        planInfo?.let { ExpoFpPlan.preloader.disposePreloadedPlan(it) }
        _presenter.value = null
        planInfo = null
    }

    // Retrieves the presenter from a preloaded plan. Call this only after
    // getPlanStatusFlow() emits ExpoFpPlanStatus.Ready.
    fun obtainPresenter() {
        val info = planInfo ?: return
        _presenter.value = ExpoFpPlan.preloader.getPreloadedPlanPresenter(info)
    }

    fun getPlanStatusFlow(): StateFlow<ExpoFpPlanStatus>? = planInfo?.planStatusFlow

    suspend fun getExhibitors(): ExpoFpResult<List<ExpoFpExhibitor>> {
        val presenter = _presenter.value
            ?: return ExpoFpResult.failure(ExpoFpError.InternalError(message = "No presenter available"))
        return presenter.exhibitorsList()
    }

    suspend fun getBooths(): ExpoFpResult<List<ExpoFpBooth>> {
        val presenter = _presenter.value
            ?: return ExpoFpResult.failure(ExpoFpError.InternalError(message = "No presenter available"))
        return presenter.boothsList()
    }

    companion object {
        private const val PLAN_KEY = "demo"
        const val ENTRANCE_BOOTH = "Entrance"

        // Hides all UI chrome on the plan so only the floor map itself is visible.
        val HIDDEN_ELEMENTS = ExpoFpElementsVisibility(
            controls = false, levels = false, header = false, overlay = false
        )
    }
}
