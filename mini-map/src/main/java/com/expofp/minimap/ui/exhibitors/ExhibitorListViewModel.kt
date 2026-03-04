package com.expofp.minimap.ui.exhibitors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expofp.fplan.api.app.model.ExpoFpError
import com.expofp.fplan.api.app.model.ExpoFpPlanStatus
import com.expofp.fplan.api.app.model.ExpoFpResult
import com.expofp.fplan.jsInteraction.model.ExpoFpExhibitor
import com.expofp.minimap.data.PlanManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExhibitorListUiState(
    val isLoading: Boolean = true,
    val loadingProgress: Int = 0,
    val exhibitors: List<ExpoFpExhibitor> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ExhibitorListViewModel @Inject constructor(
    private val planManager: PlanManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExhibitorListUiState())
    val uiState: StateFlow<ExhibitorListUiState> = _uiState.asStateFlow()

    init {
        observePlanStatus()
    }

    // Observes the preloaded plan lifecycle: Loading → Initialization → Ready (or Error).
    // The presenter can only be obtained after the plan reaches Ready status.
    private fun observePlanStatus() {
        viewModelScope.launch {
            val statusFlow = planManager.getPlanStatusFlow() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Plan not preloaded") }
                return@launch
            }

            statusFlow.collect { status ->
                when (status) {
                    is ExpoFpPlanStatus.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, loadingProgress = status.percentage)
                        }
                    }
                    is ExpoFpPlanStatus.Initialization -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ExpoFpPlanStatus.Ready -> {
                        planManager.obtainPresenter()
                        loadExhibitors()
                    }
                    is ExpoFpPlanStatus.Error -> {
                        val message = when (val err = status.error) {
                            is ExpoFpError.InternalError -> err.message ?: "Internal error"
                            is ExpoFpError.DownloadingPlanError -> "Failed to download plan"
                            is ExpoFpError.InvalidExpoKey -> "Invalid expo key"
                            else -> "Failed to load plan"
                        }
                        _uiState.update { it.copy(isLoading = false, error = message) }
                    }
                }
            }
        }
    }

    private fun loadExhibitors() {
        viewModelScope.launch {
            when (val result = planManager.getExhibitors()) {
                is ExpoFpResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, exhibitors = result.value, error = null)
                    }
                }
                is ExpoFpResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load exhibitors")
                    }
                }
            }
        }
    }
}
