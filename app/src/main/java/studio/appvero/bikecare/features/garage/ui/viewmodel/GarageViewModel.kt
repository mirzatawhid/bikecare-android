package studio.appvero.bikecare.features.garage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.garage.data.repository.BikeException
import studio.appvero.bikecare.features.garage.data.repository.BikeFailure
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeFormErrors
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeFormState
import studio.appvero.bikecare.features.garage.ui.screen.GarageEvent
import studio.appvero.bikecare.features.garage.ui.screen.GarageSideEffect
import studio.appvero.bikecare.features.garage.ui.screen.GarageUiState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GarageViewModel @Inject constructor(
    private val repository: BikeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GarageUiState>(GarageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // The sheet is independent of list loading; null means it is closed.
    private val _addBikeForm = MutableStateFlow<AddBikeFormState?>(null)
    val addBikeForm = _addBikeForm.asStateFlow()

    // Kept as part of the route contract for future one-off garage effects.
    private val _sideEffect = MutableSharedFlow<GarageSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private var loadJob: Job? = null

    init {
        loadBikes()
    }

    fun onEvent(event: GarageEvent) {
        when (event) {
            GarageEvent.Retry -> loadBikes()
            GarageEvent.OpenAddBikeSheet -> showAddBikeSheet()
            GarageEvent.DismissAddBikeSheet -> dismissAddBikeSheet()
            is GarageEvent.MakeAndModelChanged -> updateForm { copy(makeAndModel = event.value) }
            is GarageEvent.NicknameChanged -> updateForm { copy(nickname = event.value) }
            is GarageEvent.ModelYearChanged -> updateForm { copy(modelYear = event.value) }
            is GarageEvent.EngineCapacityChanged -> updateForm { copy(engineCapacityCc = event.value) }
            is GarageEvent.OdometerChanged -> updateForm { copy(odometer = event.value) }
            is GarageEvent.RegistrationNumberChanged -> updateForm { copy(registrationNumber = event.value) }
            GarageEvent.SaveBike -> saveBike()
        }
    }

    private fun showAddBikeSheet() {
        if (_addBikeForm.value == null) _addBikeForm.value = AddBikeFormState()
    }

    private fun dismissAddBikeSheet() {
        if (_addBikeForm.value?.isSaving != true) {
            _addBikeForm.value = null
        }
    }

    private fun updateForm(update: AddBikeFormState.() -> AddBikeFormState) {
        val form = _addBikeForm.value ?: return
        if (!form.isSaving) {
            _addBikeForm.value = form.update().copy(
                errors = AddBikeFormErrors(),
                submissionError = null,
            )
        }
    }

    private fun saveBike() {
        val form = _addBikeForm.value ?: return
        if (form.isSaving) return

        val validation = form.validate()
        if (validation.errors != AddBikeFormErrors()) {
            _addBikeForm.value = form.copy(errors = validation.errors)
            return
        }

        val bike = Bike(
            id = UUID.randomUUID().toString(),
            brand = validation.brand.orEmpty(),
            model = validation.model.orEmpty(),
            nickname = form.nickname.trim().ifBlank { null },
            year = form.modelYear.toInt(),
            engineCapacityCc = form.engineCapacityCc.toIntOrNull(),
            registrationNumber = form.registrationNumber.trim(),
            initialOdometer = form.odometer.toLong(),
            currentOdometer = form.odometer.toLong(),
        )
        _addBikeForm.value = form.copy(isSaving = true, submissionError = null)
        viewModelScope.launch {
            try {
                repository.addBike(bike)
                _addBikeForm.value = null
                _sideEffect.emit(GarageSideEffect.BikeAdded)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _addBikeForm.value = form.copy(
                    isSaving = false,
                    submissionError = error.toAddBikeError(),
                )
            }
        }
    }

    private fun AddBikeFormState.validate(): ValidatedBike {
        val nameParts = makeAndModel.trim().split(Regex("\\s+"), limit = 2)
        val year = modelYear.toIntOrNull()
        val engineCapacity = engineCapacityCc.toIntOrNull()
        val odometerValue = odometer.toLongOrNull()
        val registration = registrationNumber.trim()
        val errors = AddBikeFormErrors(
            makeAndModel = if (nameParts.size < 2 || nameParts.any { it.isBlank() }) {
                R.string.garage_make_model_error
            } else null,
            modelYear = if (year == null || year !in 1885..2100) R.string.garage_year_error else null,
            engineCapacityCc = if (engineCapacityCc.isNotBlank() && (engineCapacity == null || engineCapacity !in 1..3000)) {
                R.string.garage_engine_error
            } else null,
            odometer = if (odometerValue == null || odometerValue !in 0..10_000_000) {
                R.string.garage_odometer_error
            } else null,
            registrationNumber = if (registration.length > 50) R.string.garage_registration_error else null,
        )
        return ValidatedBike(errors, nameParts.getOrNull(0), nameParts.getOrNull(1))
    }

    private fun Throwable.toAddBikeError(): Int = when ((this as? BikeException)?.failure) {
        BikeFailure.Network -> R.string.garage_save_network_error
        BikeFailure.PermissionDenied -> R.string.garage_permission_error
        BikeFailure.AuthenticationExpired -> R.string.garage_auth_error
        BikeFailure.InvalidData -> R.string.garage_save_invalid_error
        else -> R.string.garage_save_generic_error
    }

    private fun loadBikes() {
        if (loadJob?.isActive == true) return
        _uiState.value = GarageUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                repository.observeUserBikes().collect { bikes ->
                    _uiState.value = if (bikes.isEmpty()) GarageUiState.Empty else GarageUiState.Content(bikes)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = GarageUiState.Error(message = when ((error as? BikeException)?.failure) {
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

private data class ValidatedBike(
    val errors: AddBikeFormErrors,
    val brand: String?,
    val model: String?,
)
