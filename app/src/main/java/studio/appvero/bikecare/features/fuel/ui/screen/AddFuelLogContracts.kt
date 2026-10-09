package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.garage.domain.model.Bike

enum class FuelPicker { BIKE, DATE }

data class AddFuelLogUiState(
    val bikes: List<Bike> = emptyList(),
    val bikesLoading: Boolean = true,
    @param:StringRes val bikesError: Int? = null,
    val bikeId: String = "",
    val date: Long = 0,
    val odometer: String = "",
    val fuelType: String = "",
    val quantity: String = "",
    val pricePerLiter: String = "",
    val totalCost: String = "",
    val station: String = "",
    val fullTank: Boolean = false,
    val notes: String = "",
    val openPicker: FuelPicker? = null,
    @param:StringRes val bikeError: Int? = null,
    @param:StringRes val dateError: Int? = null,
    @param:StringRes val odometerError: Int? = null,
    @param:StringRes val fuelTypeError: Int? = null,
    @param:StringRes val quantityError: Int? = null,
    @param:StringRes val priceError: Int? = null,
    @param:StringRes val stationError: Int? = null,
    @param:StringRes val notesError: Int? = null,
    @param:StringRes val error: Int? = null,
    val isSaving: Boolean = false,
)

sealed interface AddFuelLogEvent {
    data class BikeSelected(val id: String) : AddFuelLogEvent
    data class DateSelected(val value: Long) : AddFuelLogEvent
    data class OdometerChanged(val value: String) : AddFuelLogEvent
    data class FuelTypeChanged(val value: String) : AddFuelLogEvent
    data class QuantityChanged(val value: String) : AddFuelLogEvent
    data class PriceChanged(val value: String) : AddFuelLogEvent
    data class StationChanged(val value: String) : AddFuelLogEvent
    data class FullTankChanged(val value: Boolean) : AddFuelLogEvent
    data class NotesChanged(val value: String) : AddFuelLogEvent
    data class OpenPicker(val value: FuelPicker) : AddFuelLogEvent
    data object ClosePicker : AddFuelLogEvent
    data object RetryBikes : AddFuelLogEvent
    data object Submit : AddFuelLogEvent
    data object Back : AddFuelLogEvent
    data object EffectHandled : AddFuelLogEvent
}

sealed interface AddFuelLogSideEffect {
    data object NavigateBack : AddFuelLogSideEffect
}
