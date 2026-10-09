package studio.appvero.bikecare.features.fuel.ui.viewmodel

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
import studio.appvero.bikecare.features.fuel.data.repository.FuelLogException
import studio.appvero.bikecare.features.fuel.data.repository.FuelLogFailure
import studio.appvero.bikecare.features.fuel.data.repository.FuelLogRepository
import studio.appvero.bikecare.features.fuel.ui.screen.FuelLogListEvent
import studio.appvero.bikecare.features.fuel.ui.screen.FuelLogListSideEffect
import studio.appvero.bikecare.features.fuel.ui.screen.FuelLogListUiState
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import javax.inject.Inject

@HiltViewModel
class FuelLogListViewModel @Inject constructor(
    private val repository: FuelLogRepository,
    private val bikes: BikeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FuelLogListUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<FuelLogListSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var loadJob: Job? = null

    init { loadLogs() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: FuelLogListEvent) {
        when (event) {
            FuelLogListEvent.Retry -> loadLogs()
            FuelLogListEvent.AddLog -> _sideEffect.tryEmit(FuelLogListSideEffect.NavigateToAddLog)
            FuelLogListEvent.Back -> _sideEffect.tryEmit(FuelLogListSideEffect.NavigateBack)
            FuelLogListEvent.EffectHandled -> _sideEffect.resetReplayCache()
        }
    }

    private fun loadLogs() {
        if (loadJob?.isActive == true) return
        _uiState.value = FuelLogListUiState()
        loadJob = viewModelScope.launch {
            try {
                combine(repository.observeRecentLogs(), bikes.observeUserBikes()) { logs, bikeList ->
                    FuelLogListUiState(logs = logs,
                        bikeNames = bikeList.associate { it.id to "${it.brand} ${it.model}" }, isLoading = false)
                }.collect { _uiState.value = it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = FuelLogListUiState(isLoading = false, error = fuelLogError(error))
            }
        }
    }
}

internal fun fuelLogError(error: Exception): Int = when ((error as? FuelLogException)?.failure) {
    FuelLogFailure.Network -> R.string.fuel_network_error
    FuelLogFailure.PermissionDenied -> R.string.fuel_permission_error
    FuelLogFailure.AuthenticationExpired -> R.string.fuel_auth_error
    FuelLogFailure.InvalidData -> R.string.fuel_data_error
    else -> R.string.fuel_generic_error
}
