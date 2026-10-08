package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.maintenance.data.repository.MaintenanceException
import studio.appvero.bikecare.features.maintenance.data.repository.MaintenanceFailure
import studio.appvero.bikecare.features.maintenance.data.repository.MaintenanceRepository
import studio.appvero.bikecare.features.maintenance.ui.screen.MaintenanceEvent
import studio.appvero.bikecare.features.maintenance.ui.screen.MaintenanceSideEffect
import studio.appvero.bikecare.features.maintenance.ui.screen.MaintenanceUiState
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: MaintenanceRepository,
    private val bikes: BikeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<MaintenanceSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var loadJob: Job? = null

    init { loadLogs() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: MaintenanceEvent) {
        when (event) {
            MaintenanceEvent.Retry -> loadLogs()
            MaintenanceEvent.AddLog -> _sideEffect.tryEmit(MaintenanceSideEffect.NavigateToAddLog)
            MaintenanceEvent.EffectHandled -> _sideEffect.resetReplayCache()
        }
    }

    private fun loadLogs() {
        if (loadJob?.isActive == true) return
        _uiState.value = MaintenanceUiState()
        loadJob = viewModelScope.launch {
            try {
                combine(repository.observeRecentLogs(), bikes.observeUserBikes()) { logs, bikeList ->
                    MaintenanceUiState(logs = logs, bikeNames = bikeList.associate { it.id to "${it.brand} ${it.model}" },
                        isLoading = false)
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = MaintenanceUiState(isLoading = false, error = maintenanceError(error))
            }
        }
    }
}

internal fun maintenanceError(error: Exception): Int = when ((error as? MaintenanceException)?.failure) {
    MaintenanceFailure.Network -> R.string.maintenance_network_error
    MaintenanceFailure.PermissionDenied -> R.string.maintenance_permission_error
    MaintenanceFailure.AuthenticationExpired -> R.string.maintenance_auth_error
    MaintenanceFailure.InvalidData -> R.string.maintenance_data_error
    else -> R.string.maintenance_generic_error
}
