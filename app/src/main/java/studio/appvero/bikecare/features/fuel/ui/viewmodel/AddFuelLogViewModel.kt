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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.fuel.data.repository.FuelLogRepository
import studio.appvero.bikecare.features.fuel.domain.model.FuelLog
import studio.appvero.bikecare.features.fuel.ui.screen.AddFuelLogEvent
import studio.appvero.bikecare.features.fuel.ui.screen.AddFuelLogSideEffect
import studio.appvero.bikecare.features.fuel.ui.screen.AddFuelLogUiState
import studio.appvero.bikecare.features.garage.data.repository.BikeException
import studio.appvero.bikecare.features.garage.data.repository.BikeFailure
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddFuelLogViewModel @Inject constructor(
    private val bikes: BikeRepository,
    private val fuelLogs: FuelLogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddFuelLogUiState(
        date = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    ))
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<AddFuelLogSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var bikesJob: Job? = null

    init { loadBikes() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: AddFuelLogEvent) {
        if (event == AddFuelLogEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.isSaving) return
        when (event) {
            is AddFuelLogEvent.BikeSelected -> _uiState.update { it.copy(bikeId = event.id, bikeError = null, openPicker = null) }
            is AddFuelLogEvent.DateSelected -> _uiState.update { it.copy(date = event.value, dateError = null, openPicker = null) }
            is AddFuelLogEvent.OdometerChanged -> _uiState.update { it.copy(odometer = event.value, odometerError = null) }
            is AddFuelLogEvent.FuelTypeChanged -> _uiState.update { it.copy(fuelType = event.value, fuelTypeError = null) }
            is AddFuelLogEvent.QuantityChanged -> _uiState.update {
                it.copy(quantity = event.value, quantityError = null, totalCost = total(it.pricePerLiter, event.value))
            }
            is AddFuelLogEvent.PriceChanged -> _uiState.update {
                it.copy(pricePerLiter = event.value, priceError = null, totalCost = total(event.value, it.quantity))
            }
            is AddFuelLogEvent.StationChanged -> _uiState.update { it.copy(station = event.value, stationError = null) }
            is AddFuelLogEvent.FullTankChanged -> _uiState.update { it.copy(fullTank = event.value) }
            is AddFuelLogEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value, notesError = null) }
            is AddFuelLogEvent.OpenPicker -> _uiState.update { it.copy(openPicker = event.value) }
            AddFuelLogEvent.ClosePicker -> _uiState.update { it.copy(openPicker = null) }
            AddFuelLogEvent.RetryBikes -> loadBikes()
            AddFuelLogEvent.Submit -> submit()
            AddFuelLogEvent.Back -> _sideEffect.tryEmit(AddFuelLogSideEffect.NavigateBack)
            AddFuelLogEvent.EffectHandled -> Unit
        }
    }

    private fun loadBikes() {
        if (bikesJob?.isActive == true) return
        _uiState.update { it.copy(bikesLoading = true, bikesError = null) }
        bikesJob = viewModelScope.launch {
            try {
                bikes.observeUserBikes().collect { items ->
                    val active = items.filter { it.isActive }
                    _uiState.update { current -> current.copy(bikes = active,
                        bikeId = current.bikeId.takeIf { id -> active.any { it.id == id } } ?: "",
                        bikesLoading = false, bikesError = null) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(bikesLoading = false, bikesError = when ((error as? BikeException)?.failure) {
                    BikeFailure.Network -> R.string.fuel_network_error
                    BikeFailure.PermissionDenied -> R.string.fuel_permission_error
                    BikeFailure.AuthenticationExpired -> R.string.fuel_auth_error
                    else -> R.string.fuel_bikes_error
                }) }
            }
        }
    }

    private fun submit() {
        val form = _uiState.value
        val odometer = form.odometer.ascii().toLongOrNull()
        val quantity = form.quantity.decimal()
        val price = form.pricePerLiter.decimal()
        val cost = quantity?.multiply(price ?: BigDecimal.ZERO)?.setScale(2, RoundingMode.HALF_UP)
        val bikeError = if (form.bikes.none { it.id == form.bikeId }) R.string.fuel_choose_bike else null
        val dateError = if (form.date !in 0..4102444800000L) R.string.fuel_date_invalid else null
        val odometerError = if (odometer == null || odometer !in 0..10_000_000) R.string.fuel_odometer_invalid else null
        val typeError = if (form.fuelType.trim().length !in 1..50) R.string.fuel_type_invalid else null
        val quantityError = if (quantity == null || quantity <= BigDecimal.ZERO ||
            quantity > BigDecimal("1000") || quantity.scale() > 3) R.string.fuel_quantity_invalid else null
        val priceError = if (price == null || price <= BigDecimal.ZERO || price > BigDecimal("100000") ||
            price.scale() > 2 || cost == null || cost > BigDecimal("100000000")) R.string.fuel_price_invalid else null
        val stationError = if (form.station.trim().length > 100) R.string.fuel_station_invalid else null
        val notesError = if (form.notes.trim().length > 1000) R.string.fuel_notes_invalid else null
        _uiState.update { it.copy(bikeError = bikeError, dateError = dateError, odometerError = odometerError,
            fuelTypeError = typeError, quantityError = quantityError, priceError = priceError,
            stationError = stationError, notesError = notesError, error = null) }
        if (listOf(bikeError, dateError, odometerError, typeError, quantityError, priceError,
                stationError, notesError).any { it != null }) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                fuelLogs.addLog(FuelLog(id = UUID.randomUUID().toString(), bikeId = form.bikeId,
                    date = form.date, odometer = odometer!!, fuelType = form.fuelType.trim(),
                    quantity = quantity!!.toDouble(), pricePerLiter = price!!.toDouble(),
                    totalCost = cost!!.toDouble(), station = form.station.trim(),
                    fullTank = form.fullTank, notes = form.notes.trim()))
                _sideEffect.emit(AddFuelLogSideEffect.NavigateBack)
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isSaving = false) }
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(isSaving = false, error = fuelLogError(error)) }
            }
        }
    }

    private fun total(price: String, quantity: String): String {
        val p = price.decimal() ?: return ""
        val q = quantity.decimal() ?: return ""
        return p.multiply(q).setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    private fun String.decimal(): BigDecimal? = ascii().takeIf { it.matches(Regex("[0-9]+(\\.[0-9]+)?")) }
        ?.toBigDecimalOrNull()

    private fun String.ascii(): String = trim().map { char ->
        val digit = Character.digit(char, 10)
        if (digit >= 0) ('0'.code + digit).toChar() else char
    }.joinToString("")
}
