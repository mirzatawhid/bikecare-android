package studio.appvero.bikecare.features.garage.ui.screen

import androidx.annotation.StringRes

data class AddBikeUiState(
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val registrationNumber: String = "",
    val initialOdometer: String = "",
    val currentOdometer: String = "",
    @param:StringRes val brandError: Int? = null,
    @param:StringRes val modelError: Int? = null,
    @param:StringRes val yearError: Int? = null,
    @param:StringRes val registrationError: Int? = null,
    @param:StringRes val initialOdometerError: Int? = null,
    @param:StringRes val currentOdometerError: Int? = null,
    @param:StringRes val error: Int? = null,
    val isSaving: Boolean = false,
)

sealed interface AddBikeEvent {
    data class BrandChanged(val value: String) : AddBikeEvent
    data class ModelChanged(val value: String) : AddBikeEvent
    data class YearChanged(val value: String) : AddBikeEvent
    data class RegistrationChanged(val value: String) : AddBikeEvent
    data class InitialOdometerChanged(val value: String) : AddBikeEvent
    data class CurrentOdometerChanged(val value: String) : AddBikeEvent
    data object Submit : AddBikeEvent
    data object Back : AddBikeEvent
    data object EffectHandled : AddBikeEvent
}

sealed interface AddBikeSideEffect {
    data object NavigateBack : AddBikeSideEffect
}
