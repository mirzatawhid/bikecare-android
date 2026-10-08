package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.garage.data.repository.BikeException
import studio.appvero.bikecare.features.garage.data.repository.BikeFailure
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.maintenance.data.repository.MaintenanceRepository
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceLog
import studio.appvero.bikecare.features.maintenance.ui.screen.AddMaintenanceEvent
import studio.appvero.bikecare.features.maintenance.ui.screen.AddMaintenanceSideEffect
import studio.appvero.bikecare.features.maintenance.ui.screen.AddMaintenanceUiState
import studio.appvero.bikecare.features.maintenance.ui.screen.MaintenancePicker
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddMaintenanceViewModel @Inject constructor(
    private val bikes: BikeRepository,
    private val maintenance: MaintenanceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddMaintenanceUiState(
        date = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    ))
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<AddMaintenanceSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var bikesJob: Job? = null

    init { loadBikes() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: AddMaintenanceEvent) {
        if (event == AddMaintenanceEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.isSaving) return
        when (event) {
            is AddMaintenanceEvent.BikeSelected -> _uiState.update { it.copy(bikeId = event.id, bikeError = null, openPicker = null) }
            is AddMaintenanceEvent.TitleChanged -> _uiState.update { it.copy(title = event.value, titleError = null) }
            is AddMaintenanceEvent.CategorySelected -> _uiState.update { it.copy(category = event.value, openPicker = null) }
            is AddMaintenanceEvent.ServiceTypeSelected -> _uiState.update { it.copy(serviceType = event.value, openPicker = null) }
            is AddMaintenanceEvent.DateSelected -> _uiState.update { it.copy(date = event.value, dateError = null, openPicker = null) }
            is AddMaintenanceEvent.OdometerChanged -> _uiState.update { it.copy(odometer = event.value, odometerError = null) }
            is AddMaintenanceEvent.CostChanged -> _uiState.update { it.copy(cost = event.value, costError = null) }
            is AddMaintenanceEvent.ProviderChanged -> _uiState.update { it.copy(provider = event.value, providerError = null) }
            is AddMaintenanceEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value, descriptionError = null) }
            is AddMaintenanceEvent.NextOdometerChanged -> _uiState.update { it.copy(nextServiceOdometer = event.value, nextOdometerError = null) }
            is AddMaintenanceEvent.NextDateSelected -> _uiState.update { it.copy(nextServiceDate = event.value, nextDateError = null, openPicker = null) }
            is AddMaintenanceEvent.OpenPicker -> _uiState.update { it.copy(openPicker = event.value) }
            AddMaintenanceEvent.ClosePicker -> _uiState.update { it.copy(openPicker = null) }
            AddMaintenanceEvent.RetryBikes -> loadBikes()
            AddMaintenanceEvent.Submit -> submit()
            AddMaintenanceEvent.Back -> _sideEffect.tryEmit(AddMaintenanceSideEffect.NavigateBack)
            AddMaintenanceEvent.EffectHandled -> Unit
        }
    }

    private fun loadBikes() {
        if (bikesJob?.isActive == true) return
        _uiState.update { it.copy(bikesLoading = true, bikesError = null) }
        bikesJob = viewModelScope.launch {
            try {
                bikes.observeUserBikes().collect { items ->
                    _uiState.update { current ->
                        current.copy(
                            bikes = items.filter { it.isActive },
                            bikeId = current.bikeId.takeIf { selected -> items.any { it.id == selected && it.isActive } } ?: "",
                            bikesLoading = false,
                            bikesError = null,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(bikesLoading = false, bikesError = when ((error as? BikeException)?.failure) {
                    BikeFailure.Network -> R.string.maintenance_network_error
                    BikeFailure.PermissionDenied -> R.string.maintenance_permission_error
                    BikeFailure.AuthenticationExpired -> R.string.maintenance_auth_error
                    else -> R.string.maintenance_bikes_error
                }) }
            }
        }
    }

    private fun submit() {
        val form = _uiState.value

        Log.d(
            "AddMaintenanceVM",
            "submit: bikeId=${form.bikeId}, title=${form.title}, date=${form.date}, " +
                    "odometer=${form.odometer}, cost=${form.cost}, " +
                    "nextOdometer=${form.nextServiceOdometer}, nextDate=${form.nextServiceDate}"
        )

        val odometer = form.odometer.toAsciiDigits().toLongOrNull()
        val cost = form.cost.toAsciiDigits().toLongOrNull()
        val nextOdometer = form.nextServiceOdometer
            .takeIf { it.isNotBlank() }
            ?.toAsciiDigits()
            ?.toLongOrNull()

        Log.d(
            "AddMaintenanceVM",
            "submit parsed: odometer=$odometer, cost=$cost, nextOdometer=$nextOdometer"
        )

        val bikeError =
            if (form.bikes.none { it.id == form.bikeId }) {
                R.string.maintenance_choose_bike
            } else {
                null
            }

        val titleError =
            if (form.title.trim().length !in 1..100) {
                R.string.maintenance_title_invalid
            } else {
                null
            }

        val dateError =
            if (form.date !in 0..4102444800000L) {
                R.string.maintenance_date_invalid
            } else {
                null
            }

        val odometerError =
            if (odometer == null || odometer !in 0..10_000_000) {
                R.string.maintenance_odometer_invalid
            } else {
                null
            }

        val costError =
            if (cost == null || cost !in 0..1_000_000_000) {
                R.string.maintenance_cost_invalid
            } else {
                null
            }

        val providerError =
            if (form.provider.trim().length > 100) {
                R.string.maintenance_provider_invalid
            } else {
                null
            }

        val descriptionError =
            if (form.description.trim().length > 1000) {
                R.string.maintenance_description_invalid
            } else {
                null
            }

        val nextOdometerError =
            if (
                form.nextServiceOdometer.isNotBlank() &&
                (
                        nextOdometer == null ||
                                nextOdometer > 10_000_000 ||
                                (odometer != null && nextOdometer < odometer)
                        )
            ) {
                R.string.maintenance_next_odometer_invalid
            } else {
                null
            }

        val nextDateError =
            if (
                form.nextServiceDate != null &&
                form.nextServiceDate !in form.date..4102444800000L
            ) {
                R.string.maintenance_next_date_invalid
            } else {
                null
            }

        _uiState.update {
            it.copy(
                bikeError = bikeError,
                titleError = titleError,
                dateError = dateError,
                odometerError = odometerError,
                costError = costError,
                providerError = providerError,
                descriptionError = descriptionError,
                nextOdometerError = nextOdometerError,
                nextDateError = nextDateError,
                error = null,
            )
        }

        val hasValidationError = listOf(
            bikeError,
            titleError,
            dateError,
            odometerError,
            costError,
            providerError,
            descriptionError,
            nextOdometerError,
            nextDateError,
        ).any { it != null }

        if (hasValidationError) {
            Log.d(
                "AddMaintenanceVM",
                "submit validation failed: " +
                        "bike=$bikeError, title=$titleError, date=$dateError, " +
                        "odometer=$odometerError, cost=$costError, " +
                        "provider=$providerError, description=$descriptionError, " +
                        "nextOdometer=$nextOdometerError, nextDate=$nextDateError"
            )
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val log = MaintenanceLog(
                    id = UUID.randomUUID().toString(),
                    bikeId = form.bikeId,
                    title = form.title.trim(),
                    category = form.category,
                    serviceType = form.serviceType,
                    date = form.date,
                    odometer = odometer!!,
                    cost = cost!!,
                    provider = form.provider.trim(),
                    description = form.description.trim(),
                    nextServiceOdometer = nextOdometer,
                    nextServiceDate = form.nextServiceDate,
                )

                Log.d(
                    "AddMaintenanceVM",
                    "submit saving MaintenanceLog: $log"
                )

                maintenance.addLog(log)

                Log.d(
                    "AddMaintenanceVM",
                    "submit success: logId=${log.id}, bikeId=${log.bikeId}"
                )

                _sideEffect.emit(AddMaintenanceSideEffect.NavigateBack)
            } catch (cancelled: CancellationException) {
                Log.d("AddMaintenanceVM", "submit cancelled")

                _uiState.update { it.copy(isSaving = false) }
                throw cancelled
            } catch (error: Exception) {
                Log.e(
                    "AddMaintenanceVM",
                    "submit failed: ${error.message}",
                    error,
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = maintenanceError(error),
                    )
                }
            }
        }
    }

    private fun String.toAsciiDigits(): String = map { character ->
        val digit = Character.digit(character, 10)
        if (digit >= 0) ('0'.code + digit).toChar() else character
    }.joinToString("")
}
