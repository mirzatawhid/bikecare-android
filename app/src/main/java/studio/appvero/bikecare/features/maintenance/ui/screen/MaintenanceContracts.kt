package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceLog

data class MaintenanceUiState(
    val logs: List<MaintenanceLog> = emptyList(),
    val bikeNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    @param:StringRes val error: Int? = null,
)

sealed interface MaintenanceEvent {
    data object Retry : MaintenanceEvent
    data object AddLog : MaintenanceEvent
    data object EffectHandled : MaintenanceEvent
}

sealed interface MaintenanceSideEffect {
    data object NavigateToAddLog : MaintenanceSideEffect
}
