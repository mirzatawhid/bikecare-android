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
import java.util.UUID
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField.*
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val bikeRepository: BikeRepository,
    private val repository: MaintenanceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MaintenanceUiState>(MaintenanceUiState.Loading())
    val uiState = _uiState.asStateFlow()
    private val _logServiceForm = MutableStateFlow<LogServiceFormState?>(null)
    val logServiceForm = _logServiceForm.asStateFlow()
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
            is MaintenanceEvent.LogService -> openService(event.maintenanceId)
            MaintenanceEvent.DismissLogService -> if (_logServiceForm.value?.isSaving != true) _logServiceForm.value = null
            is MaintenanceEvent.ServiceFieldChanged -> updateForm {
                copy(fields = fields + (event.field to event.value))
            }
            is MaintenanceEvent.SelectService -> updateForm {
                val item = services.firstOrNull { it.id == event.maintenanceId }
                copy(maintenanceId = item?.id, fields = fields + mapOf(
                    Name to item?.name.orEmpty(), RepeatKm to item?.repeatEveryKm?.toString().orEmpty(),
                    RepeatDays to item?.repeatEveryDays?.toString().orEmpty(), DueDate to "", DueOdometer to "",
                ), reminderEnabled = item?.let { it.repeatEveryKm != null || it.repeatEveryDays != null } == true)
            }
            is MaintenanceEvent.SetReminder -> updateForm { copy(reminderEnabled = event.enabled) }
            MaintenanceEvent.SaveService -> saveService()
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
                    MaintenanceEvent.Reminder -> bikeId?.let { MaintenanceSideEffect.Reminder(it) }
                    is MaintenanceEvent.OpenItem -> bikeId?.let { MaintenanceSideEffect.OpenItem(it, event.maintenanceId) }
                    else -> null
                } ?: return
                navigationPending = true
                viewModelScope.launch { _sideEffect.emit(effect) }
            }
        }
    }

    private fun openService(maintenanceId: String?) {
        if (_logServiceForm.value != null) return
        val bike = _uiState.value.bike ?: return
        val items = _uiState.value.retainedContent()?.items.orEmpty().map { it.item }
        val item = items.firstOrNull { it.id == maintenanceId }
        if (maintenanceId != null && item == null) return
        _logServiceForm.value = LogServiceFormState(
            id = UUID.randomUUID().toString(), bike = bike, maintenanceId = item?.id, services = items,
            reminderEnabled = item?.let { it.repeatEveryKm != null || it.repeatEveryDays != null } == true,
            fields = mapOf(Name to item?.name.orEmpty(), ServiceDate to LocalDate.now().toString(),
                Odometer to bike.currentOdometer.toString(), RepeatKm to item?.repeatEveryKm?.toString().orEmpty(),
                RepeatDays to item?.repeatEveryDays?.toString().orEmpty()),
        )
    }

    private fun updateForm(update: LogServiceFormState.() -> LogServiceFormState) {
        val form = _logServiceForm.value ?: return
        if (!form.isSaving) _logServiceForm.value = form.update().copy(errors = emptyMap(), submissionError = null)
    }

    private fun saveService() {
        val form = _logServiceForm.value ?: return
        if (form.isSaving) return
        val validation = form.validateService()
        val log = validation.log
        if (log == null) {
            _logServiceForm.value = form.copy(errors = validation.errors)
            return
        }
        _logServiceForm.value = form.copy(isSaving = true, errors = emptyMap(), submissionError = null)
        viewModelScope.launch {
            try {
                repository.logService(log)
                if (_logServiceForm.value?.id == form.id) _logServiceForm.value = null
                // Counts, priority and odometer come exclusively from repository observations.
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (_logServiceForm.value?.id == form.id) _logServiceForm.value = form.copy(
                    submissionError = when ((error as? MaintenanceException)?.failure) {
                        MaintenanceFailure.Network -> R.string.garage_save_network_error
                        MaintenanceFailure.PermissionDenied -> R.string.garage_permission_error
                        MaintenanceFailure.AuthenticationExpired -> R.string.garage_auth_error
                        MaintenanceFailure.InvalidData -> R.string.service_save_invalid
                        else -> R.string.service_save_error
                    },
                )
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
                    if (_logServiceForm.value?.bike?.id != bike?.id) _logServiceForm.value = null
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
                if (clearData) _logServiceForm.value = null
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
