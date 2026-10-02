package studio.appvero.bikecare.features.garage.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.garage.domain.model.Bike

data class GarageUiState(
    val bikes: List<Bike> = emptyList(),
    val isLoading: Boolean = true,
    @param:StringRes val error: Int? = null,
    val isAddBikeSheetVisible: Boolean = false,
    val addBikeForm: AddBikeFormState = AddBikeFormState(),
)

data class AddBikeFormState(
    val makeAndModel: String = "",
    val nickname: String = "",
    val modelYear: String = currentModelYear().toString(),
    val engineCapacityCc: String = "",
    val odometer: String = "0",
    val registrationNumber: String = "",
    val isSaving: Boolean = false,
    val errors: AddBikeFormErrors = AddBikeFormErrors(),
    @param:StringRes val submissionError: Int? = null,
)

data class AddBikeFormErrors(
    @param:StringRes val makeAndModel: Int? = null,
    @param:StringRes val modelYear: Int? = null,
    @param:StringRes val engineCapacityCc: Int? = null,
    @param:StringRes val odometer: Int? = null,
    @param:StringRes val registrationNumber: Int? = null,
)

sealed interface GarageEvent {
    data object Retry : GarageEvent
    data object OpenAddBikeSheet : GarageEvent
    data object DismissAddBikeSheet : GarageEvent
    data class MakeAndModelChanged(val value: String) : GarageEvent
    data class NicknameChanged(val value: String) : GarageEvent
    data class ModelYearChanged(val value: String) : GarageEvent
    data class EngineCapacityChanged(val value: String) : GarageEvent
    data class OdometerChanged(val value: String) : GarageEvent
    data class RegistrationNumberChanged(val value: String) : GarageEvent
    data object SaveBike : GarageEvent
}

sealed interface GarageSideEffect {
    data object BikeAdded : GarageSideEffect
}

private fun currentModelYear(): Int = java.time.Year.now().value
