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
    val isMapExpanded: Boolean = false,
    val isDirectionsActive: Boolean = false,
    val pendingDirections: Boolean = false
)

@HiltViewModel
class ExhibitorDetailViewModel @Inject constructor(
    private val planManager: PlanManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exhibitorName: String = savedStateHandle.toRoute<ExhibitorDetail>().exhibitorName

    private val _uiState = MutableStateFlow(ExhibitorDetailUiState(exhibitorName = exhibitorName))
    val uiState: StateFlow<ExhibitorDetailUiState> = _uiState.asStateFlow()

    val presenter: StateFlow<IExpoFpPlanPresenter?> = planManager.presenter

    init {
        resolveBoothName()
    }

    // Resolves the exhibitor name to a booth name used by the SDK for selection and routing.
    // If any step fails, the detail screen still works —
    // just without booth highlighting and directions.
    private fun resolveBoothName() {
        viewModelScope.launch {
            val exhibitors = planManager.getExhibitors().getOrNull() ?: return@launch
            val exhibitor = exhibitors.find { it.name == exhibitorName } ?: return@launch
            val firstBoothId = exhibitor.booths.firstOrNull() ?: return@launch

            val booths = planManager.getBooths().getOrNull() ?: return@launch
            val booth = booths.find { it.id == firstBoothId } ?: return@launch

            // selectBooth() accepts either name or externalId.
            // Use externalId (stable system ID) when available; fall back to display name.
            val name = booth.externalId.ifEmpty { booth.name }
            _uiState.update { it.copy(boothName = name) }
            // Highlight the booth on the plan
            planManager.presenter.value?.selectBooth(name)
        }
    }

    fun onScreenEnter() {
        val presenter = planManager.presenter.value ?: return
        presenter.setElementsVisibility(PlanManager.HIDDEN_ELEMENTS)
        presenter.fitBounds()
    }

    // Clear all selections and reset the plan view when leaving the screen.
    // This ensures a clean state for the next exhibitor detail.
    fun onScreenExit() {
        val presenter = planManager.presenter.value ?: return
        presenter.selectBooth("")
        presenter.selectRoute(emptyList())
        // Reset zoom to show the entire plan
        presenter.fitBounds()
    }

    fun toggleMapExpanded() {
        val currentState = _uiState.value
        val presenter = planManager.presenter.value ?: return

        if (currentState.isMapExpanded) {
            // Collapsing: clear route if active, re-select booth, reset zoom
            if (currentState.isDirectionsActive) {
                presenter.selectRoute(emptyList())
            }
            currentState.boothName?.let { presenter.selectBooth(it) }
            presenter.fitBounds()
            _uiState.update { it.copy(isMapExpanded = false, isDirectionsActive = false) }
        } else {
            presenter.setElementsVisibility(PlanManager.HIDDEN_ELEMENTS)
            currentState.boothName?.let { presenter.selectBooth(it) }
            _uiState.update { it.copy(isMapExpanded = true) }
        }
    }

    fun toggleDirections() {
        val boothName = _uiState.value.boothName ?: return
        val presenter = planManager.presenter.value ?: return
        val currentState = _uiState.value

        if (currentState.isDirectionsActive) {
            // Clear route and re-select booth to show just the highlight
            presenter.selectRoute(emptyList())
            presenter.selectBooth(boothName)
            _uiState.update { it.copy(isDirectionsActive = false) }
        } else {
            // Build a route from the entrance to the selected booth
            presenter.selectRoute(
                from = ExpoFpRouteWaypoint.Booth(PlanManager.ENTRANCE_BOOTH),
                to = ExpoFpRouteWaypoint.Booth(boothName)
            )
            _uiState.update { it.copy(isDirectionsActive = true) }
        }
    }

    // Expands the map and defers directions until the expand animation completes.
    // We set pendingDirections = true; the Screen calls consumePendingDirections()
    // after the animation finishes so selectRoute() runs on the full-size viewport.
    fun expandWithDirections() {
        val presenter = planManager.presenter.value ?: return
        presenter.setElementsVisibility(PlanManager.HIDDEN_ELEMENTS)
        _uiState.update { it.copy(isMapExpanded = true, pendingDirections = true) }
    }

    fun consumePendingDirections() {
        if (_uiState.value.pendingDirections) {
            _uiState.update { it.copy(pendingDirections = false) }
            toggleDirections()
        }
    }
}
