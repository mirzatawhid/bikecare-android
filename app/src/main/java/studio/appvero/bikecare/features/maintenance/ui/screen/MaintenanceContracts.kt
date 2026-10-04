package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceAssessment

sealed interface MaintenanceUiState {
    val bike: Bike?

    data class Loading(
        override val bike: Bike? = null,
        val previousContent: Content? = null,
    ) : MaintenanceUiState

    data object NoBike : MaintenanceUiState {
        override val bike: Bike? = null
    }

    data class Empty(override val bike: Bike) : MaintenanceUiState

    data class Content(
        override val bike: Bike,
        val items: List<MaintenanceAssessment>,
        val priorityItem: MaintenanceAssessment,
        val overdueCount: Int,
        val dueSoonCount: Int,
        val upToDateCount: Int,
    ) : MaintenanceUiState

    data class Error(
        @param:StringRes val message: Int,
        override val bike: Bike? = null,
        val previousContent: Content? = null,
    ) : MaintenanceUiState
}

sealed interface MaintenanceEvent {
    data object Back : MaintenanceEvent
    data object OpenGarage : MaintenanceEvent
    data object Retry : MaintenanceEvent
    data object RefreshDate : MaintenanceEvent
    data object EffectHandled : MaintenanceEvent
    data class LogService(val maintenanceId: String? = null) : MaintenanceEvent
    data object Reminder : MaintenanceEvent
    data class OpenItem(val maintenanceId: String) : MaintenanceEvent
}

sealed interface MaintenanceSideEffect {
    data object Back : MaintenanceSideEffect
    data object OpenGarage : MaintenanceSideEffect
    data class LogService(val bikeId: String, val maintenanceId: String?) : MaintenanceSideEffect
    data class Reminder(val bikeId: String) : MaintenanceSideEffect
    data class OpenItem(val bikeId: String, val maintenanceId: String) : MaintenanceSideEffect
}
