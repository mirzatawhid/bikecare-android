package studio.appvero.bikecare.features.garage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeEvent
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeSideEffect
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeUiState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddBikeViewModel @Inject constructor(private val repository: BikeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddBikeUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<AddBikeSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: AddBikeEvent) {
        if (event == AddBikeEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.isSaving) return
        when (event) {
            is AddBikeEvent.BrandChanged -> _uiState.update { it.copy(brand = event.value, brandError = null, error = null) }
            is AddBikeEvent.ModelChanged -> _uiState.update { it.copy(model = event.value, modelError = null, error = null) }
            is AddBikeEvent.YearChanged -> _uiState.update { it.copy(year = event.value, yearError = null, error = null) }
            is AddBikeEvent.RegistrationChanged -> _uiState.update { it.copy(registrationNumber = event.value, registrationError = null, error = null) }
            is AddBikeEvent.InitialOdometerChanged -> _uiState.update { it.copy(initialOdometer = event.value, initialOdometerError = null, currentOdometerError = null, error = null) }
            is AddBikeEvent.CurrentOdometerChanged -> _uiState.update { it.copy(currentOdometer = event.value, currentOdometerError = null, error = null) }
            AddBikeEvent.Submit -> submit()
            AddBikeEvent.Back -> _sideEffect.tryEmit(AddBikeSideEffect.NavigateBack)
            AddBikeEvent.EffectHandled -> Unit
        }
    }

    private fun submit() {
        val form = _uiState.value
        val brand = form.brand.trim()
        val model = form.model.trim()
        val registration = form.registrationNumber.trim()
        val year = form.year.toAsciiDigits().toIntOrNull()
        val initialOdometer = form.initialOdometer.toAsciiDigits().toLongOrNull()
        val currentOdometer = form.currentOdometer.toAsciiDigits().toLongOrNull()
        val brandError = when { brand.isEmpty() -> R.string.add_bike_required; brand.length > 100 -> R.string.add_bike_text_too_long; else -> null }
        val modelError = when { model.isEmpty() -> R.string.add_bike_required; model.length > 100 -> R.string.add_bike_text_too_long; else -> null }
        val yearError = if (year == null || year !in 1885..2100) R.string.add_bike_year_invalid else null
        val registrationError = if (registration.length > 50) R.string.add_bike_registration_too_long else null
        val initialError = if (initialOdometer == null || initialOdometer !in 0..10_000_000) R.string.add_bike_odometer_invalid else null
        val currentError = when {
            currentOdometer == null || currentOdometer !in 0..10_000_000 -> R.string.add_bike_odometer_invalid
            initialOdometer != null && currentOdometer < initialOdometer -> R.string.add_bike_current_odometer_invalid
            else -> null
        }
        val invalid = form.copy(
            brandError = brandError,
            modelError = modelError,
            yearError = yearError,
            registrationError = registrationError,
            initialOdometerError = initialError,
            currentOdometerError = currentError,
        )
        if (listOf(brandError, modelError, yearError, registrationError, initialError, currentError).any { it != null }) {
            _uiState.value = invalid
            return
        }

        _uiState.value = invalid.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                repository.addBike(
                    Bike(
                        id = UUID.randomUUID().toString(),
                        brand = brand,
                        model = model,
                        year = year!!,
                        registrationNumber = registration,
                        initialOdometer = initialOdometer!!,
                        currentOdometer = currentOdometer!!,
                        imageUrl = null,
                        isActive = true,
                    ),
                )
                _sideEffect.emit(AddBikeSideEffect.NavigateBack)
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isSaving = false) }
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(isSaving = false, error = errorMessage(error)) }
            }
        }
    }

    private fun errorMessage(error: Exception): Int = when ((error as? BikeException)?.failure) {
        BikeFailure.Network -> R.string.garage_network_error
        BikeFailure.PermissionDenied -> R.string.garage_permission_error
        BikeFailure.AuthenticationExpired -> R.string.garage_auth_error
        BikeFailure.InvalidData -> R.string.garage_data_error
        else -> R.string.add_bike_save_error
    }

    private fun String.toAsciiDigits(): String = map { character ->
        val digit = Character.digit(character, 10)
        if (digit >= 0) ('0'.code + digit).toChar() else character
    }.joinToString("")
}
