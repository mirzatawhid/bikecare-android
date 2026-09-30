package studio.appvero.bikecare.features.garage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.garage.data.repository.*
import studio.appvero.bikecare.features.garage.ui.screen.*
import javax.inject.Inject

@HiltViewModel
class GarageViewModel @Inject constructor(private val repository: BikeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GarageUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<GarageSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var loadJob: Job? = null

    init { loadBikes() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: GarageEvent) {
        when (event) {
            GarageEvent.Retry -> loadBikes()
            GarageEvent.AddBike -> _sideEffect.tryEmit(GarageSideEffect.NavigateToAddBike)
            GarageEvent.EffectHandled -> _sideEffect.resetReplayCache()
        }
    }

    private fun loadBikes() {
        if (loadJob?.isActive == true) return
        _uiState.value = GarageUiState()
        loadJob = viewModelScope.launch {
            try {
                repository.observeUserBikes().collect { bikes ->
                    _uiState.value = GarageUiState(bikes = bikes, isLoading = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = GarageUiState(isLoading = false, error = when ((error as? BikeException)?.failure) {
                    BikeFailure.Network -> R.string.garage_network_error
                    BikeFailure.PermissionDenied -> R.string.garage_permission_error
                    BikeFailure.AuthenticationExpired -> R.string.garage_auth_error
                    BikeFailure.InvalidData -> R.string.garage_data_error
                    else -> R.string.garage_generic_error
                })
            }
        }
    }
}
