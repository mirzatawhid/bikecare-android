package studio.appvero.bikecare.features.garage.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.garage.domain.model.Bike

data class GarageUiState(
    val bikes: List<Bike> = emptyList(),
    val isLoading: Boolean = true,
    @param:StringRes val error: Int? = null,
)

sealed interface GarageEvent {
    data object Retry : GarageEvent
    data object AddBike : GarageEvent
    data object EffectHandled : GarageEvent
}

sealed interface GarageSideEffect {
    data object NavigateToAddBike : GarageSideEffect
}
