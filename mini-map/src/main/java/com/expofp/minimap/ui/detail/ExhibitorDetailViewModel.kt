package com.expofp.minimap.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.expofp.fplan.api.app.IExpoFpPlanPresenter
import com.expofp.fplan.jsInteraction.model.ExpoFpRouteWaypoint
import com.expofp.minimap.data.PlanManager
import com.expofp.minimap.navigation.ExhibitorDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExhibitorDetailUiState(
    val exhibitorName: String = "",
    val boothName: String? = null,
    val isMapExpanded: Boolean = false
)

@HiltViewModel
class ExhibitorDetailViewModel @Inject constructor(
    private val planManager: PlanManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exhibitorName: String = savedStateHandle.toRoute<ExhibitorDetail>().exhibitorName

    private val _uiState = MutableStateFlow(ExhibitorDetailUiState(exhibitorName = exhibitorName))
    val uiState: StateFlow<ExhibitorDetailUiState> = _uiState.asStateFlow()

    val miniMapPresenter: IExpoFpPlanPresenter? get() = planManager.miniMapPresenter
    val fullMapPresenter: IExpoFpPlanPresenter? get() = planManager.fullMapPresenter

    init {
        resolveBoothName()
    }

    private fun resolveBoothName() {
        viewModelScope.launch {
            val exhibitors = planManager.getExhibitors().getOrNull() ?: return@launch
            val exhibitor = exhibitors.find { it.name == exhibitorName } ?: return@launch
            val firstBoothId = exhibitor.booths.firstOrNull() ?: return@launch

            val booths = planManager.getBooths().getOrNull() ?: return@launch
            val booth = booths.find { it.id == firstBoothId } ?: return@launch

            val name = booth.externalId.ifEmpty { booth.name }
            _uiState.update { it.copy(boothName = name) }
        }
    }

    fun onScreenEnter() {
        forBothPresenters { presenter ->
            presenter.selectExhibitor(exhibitorName)
            presenter.fitBounds()
        }
    }

    fun onScreenExit() {
        planManager.miniMapPresenter?.selectExhibitor()
        planManager.miniMapPresenter?.fitBounds()
    }

    fun toggleMapExpanded() {
        val currentState = _uiState.value

        if (currentState.isMapExpanded) {
            _uiState.update { it.copy(isMapExpanded = false) }
        } else {
            planManager.fullMapPresenter?.selectExhibitor(exhibitorName)
            _uiState.update { it.copy(isMapExpanded = true) }
        }
    }

    fun onCollapseAnimationFinished() {
        val presenter = planManager.fullMapPresenter ?: return
        presenter.selectRoute(emptyList())
        presenter.selectExhibitor(exhibitorName)
    }

    fun showDirections() {
        val boothName = _uiState.value.boothName ?: return
        val presenter = planManager.fullMapPresenter ?: return
        presenter.selectRoute(
            from = ExpoFpRouteWaypoint.Booth(PlanManager.ENTRANCE_BOOTH),
            to = ExpoFpRouteWaypoint.Booth(boothName)
        )
    }

    fun expandWithDirections() {
        _uiState.update { it.copy(isMapExpanded = true) }
        showDirections()
    }

    private fun forBothPresenters(action: (IExpoFpPlanPresenter) -> Unit) {
        planManager.miniMapPresenter?.let(action)
        planManager.fullMapPresenter?.let(action)
    }
}
