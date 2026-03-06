package com.expofp.minimap.data

import com.expofp.fplan.api.app.ExpoFpPlan
import com.expofp.fplan.api.app.IExpoFpPlanPresenter
import com.expofp.fplan.api.app.model.ExpoFpError
import com.expofp.fplan.api.app.model.ExpoFpLinkType
import com.expofp.fplan.api.app.model.ExpoFpPlanParameter
import com.expofp.fplan.api.app.model.ExpoFpPlanStatus
import com.expofp.fplan.api.app.model.ExpoFpResult
import com.expofp.fplan.jsInteraction.model.ExpoFpBooth
import com.expofp.fplan.jsInteraction.model.ExpoFpElementsVisibility
import com.expofp.fplan.jsInteraction.model.ExpoFpExhibitor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanManager @Inject constructor() {

    var miniMapPresenter: IExpoFpPlanPresenter? = null
        private set

    var fullMapPresenter: IExpoFpPlanPresenter? = null
        private set

    fun createPresenters() {
        if (miniMapPresenter != null) return

        val params = listOf(
            ExpoFpPlanParameter.NoOverlay(true),
            ExpoFpPlanParameter.HideHeaderLogo(true)
        )
        miniMapPresenter = ExpoFpPlan.createPlanPresenter(
            planLink = ExpoFpLinkType.ExpoKey(PLAN_KEY),
            additionalParams = params
        )
        fullMapPresenter = ExpoFpPlan.createPlanPresenter(
            planLink = ExpoFpLinkType.ExpoKey(PLAN_KEY),
            additionalParams = params
        )
    }

    fun getMiniMapStatusFlow(): Flow<ExpoFpPlanStatus>? = miniMapPresenter?.planStatusFlow

    suspend fun getExhibitors(): ExpoFpResult<List<ExpoFpExhibitor>> {
        val presenter = miniMapPresenter
            ?: return ExpoFpResult.failure(ExpoFpError.InternalError(message = "No presenter available"))
        return presenter.exhibitorsList()
    }

    suspend fun getBooths(): ExpoFpResult<List<ExpoFpBooth>> {
        val presenter = miniMapPresenter
            ?: return ExpoFpResult.failure(ExpoFpError.InternalError(message = "No presenter available"))
        return presenter.boothsList()
    }

    companion object {
        private const val PLAN_KEY = "demo"
        const val ENTRANCE_BOOTH = "Entrance"

        val HIDDEN_ELEMENTS = ExpoFpElementsVisibility(
            controls = false, levels = false, header = false, overlay = false
        )
    }
}
