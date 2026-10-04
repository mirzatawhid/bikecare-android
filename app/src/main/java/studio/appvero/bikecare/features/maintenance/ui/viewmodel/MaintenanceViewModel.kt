package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.garage.data.repository.BikeException
import studio.appvero.bikecare.features.garage.data.repository.BikeFailure
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.maintenance.data.repository.*
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceStatus
import studio.appvero.bikecare.features.maintenance.ui.screen.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val repository: MaintenanceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MaintenanceUiState>(MaintenanceUiState.Loading())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<MaintenanceSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private val today = MutableStateFlow(LocalDate.now())
    private var loadJob: Job? = null
    private var navigationPending = false

    init {
        load()
        viewModelScope.launch {
            // Also catches a local timezone/date change while the screen remains open.
            while (true) {
                delay(60_000)
                today.value = LocalDate.now()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: MaintenanceEvent) {
        when (event) {
            MaintenanceEvent.Retry -> load()
            MaintenanceEvent.RefreshDate -> today.value = LocalDate.now()
            MaintenanceEvent.EffectHandled -> {
                _sideEffect.resetReplayCache()
                navigationPending = false
            }
            else -> {
                if (navigationPending) return
                val bikeId = _uiState.value.bike?.id
                val effect = when (event) {
                    MaintenanceEvent.Back -> MaintenanceSideEffect.Back
                    MaintenanceEvent.OpenGarage -> MaintenanceSideEffect.OpenGarage
                    is MaintenanceEvent.LogService -> bikeId?.let { MaintenanceSideEffect.LogService(it, event.maintenanceId) }
                    MaintenanceEvent.Reminder -> bikeId?.let { MaintenanceSideEffect.Reminder(it) }
                    is MaintenanceEvent.OpenItem -> bikeId?.let { MaintenanceSideEffect.OpenItem(it, event.maintenanceId) }
                    else -> null
                } ?: return
                navigationPending = true
                viewModelScope.launch { _sideEffect.emit(effect) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun load() {
        if (loadJob?.isActive == true) return
        _uiState.value = MaintenanceUiState.Loading(
            bike = _uiState.value.bike,
            previousContent = _uiState.value.retainedContent(),
        )
        loadJob = viewModelScope.launch {
            try {
                bikeRepository.observeUserBikes().flatMapLatest { bikes ->
                    // Garage has no selected-bike state. Its newest active bike is the default.
                    val bike = bikes.firstOrNull { it.isActive }
                    if (bike == null) flowOf(MaintenanceUiState.NoBike)
                    else {
                        if (_uiState.value.bike?.id != bike.id) {
                            _uiState.value = MaintenanceUiState.Loading(bike = bike)
                        }
                        combine(repository.observeMaintenance(bike.id), today) { items, date ->
                            val assessed = items.map { it.assess(date, bike.currentOdometer) }.byPriority()
                            if (assessed.isEmpty()) MaintenanceUiState.Empty(bike)
                            else MaintenanceUiState.Content(
                                bike = bike, items = assessed, priorityItem = assessed.first(),
                                overdueCount = assessed.count { it.status == MaintenanceStatus.OVERDUE },
                                dueSoonCount = assessed.count { it.status == MaintenanceStatus.DUE_SOON },
                                upToDateCount = assessed.count { it.status == MaintenanceStatus.UP_TO_DATE },
                            )
                        }
                    }
                }.collect { _uiState.value = it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val failure = (error as? MaintenanceException)?.failure
                val bikeFailure = (error as? BikeException)?.failure
                val clearData = failure == MaintenanceFailure.AuthenticationExpired ||
                    failure == MaintenanceFailure.PermissionDenied ||
                    bikeFailure == BikeFailure.AuthenticationExpired || bikeFailure == BikeFailure.PermissionDenied
                val state = _uiState.value
                _uiState.value = MaintenanceUiState.Error(
                    bike = if (clearData) null else state.bike,
                    previousContent = if (clearData) null else state.retainedContent(),
                    message = when {
                        failure == MaintenanceFailure.Network || bikeFailure == BikeFailure.Network -> R.string.garage_network_error
                        failure == MaintenanceFailure.PermissionDenied || bikeFailure == BikeFailure.PermissionDenied -> R.string.garage_permission_error
                        failure == MaintenanceFailure.AuthenticationExpired || bikeFailure == BikeFailure.AuthenticationExpired -> R.string.garage_auth_error
                        failure == MaintenanceFailure.InvalidData || bikeFailure == BikeFailure.InvalidData -> R.string.maintenance_data_error
                        else -> R.string.maintenance_read_error
                    },
                )
            }
        }
    }
}

private fun MaintenanceUiState.retainedContent(): MaintenanceUiState.Content? = when (this) {
    is MaintenanceUiState.Content -> this
    is MaintenanceUiState.Loading -> previousContent
    is MaintenanceUiState.Error -> previousContent
    is MaintenanceUiState.Empty, MaintenanceUiState.NoBike -> null
}
