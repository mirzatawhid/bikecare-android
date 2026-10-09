package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.fuel.domain.model.FuelLog

data class FuelLogListUiState(
    val logs: List<FuelLog> = emptyList(),
    val bikeNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    @param:StringRes val error: Int? = null,
)

sealed interface FuelLogListEvent {
    data object Retry : FuelLogListEvent
    data object AddLog : FuelLogListEvent
    data object Back : FuelLogListEvent
    data object EffectHandled : FuelLogListEvent
}

sealed interface FuelLogListSideEffect {
    data object NavigateToAddLog : FuelLogListSideEffect
    data object NavigateBack : FuelLogListSideEffect
}
