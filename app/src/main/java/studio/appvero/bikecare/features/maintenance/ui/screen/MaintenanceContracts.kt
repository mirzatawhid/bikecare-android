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
    data object DismissLogService : MaintenanceEvent
    data class ServiceFieldChanged(val field: ServiceField, val value: String) : MaintenanceEvent
    data class SelectService(val maintenanceId: String?) : MaintenanceEvent
    data class SetReminder(val enabled: Boolean) : MaintenanceEvent
    data object SaveService : MaintenanceEvent
    data object Reminder : MaintenanceEvent
    data class OpenItem(val maintenanceId: String) : MaintenanceEvent
}

sealed interface MaintenanceSideEffect {
    data object Back : MaintenanceSideEffect
    data object OpenGarage : MaintenanceSideEffect
    data class Reminder(val bikeId: String) : MaintenanceSideEffect
    data class OpenItem(val bikeId: String, val maintenanceId: String) : MaintenanceSideEffect
}

enum class ServiceField { Name, ServiceDate, Odometer, Cost, Workshop, Notes, DueOdometer, DueDate, RepeatKm, RepeatDays, ReminderDue }

data class LogServiceFormState(
    val id: String,
    val bike: Bike,
    val maintenanceId: String? = null,
    val services: List<studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem> = emptyList(),
    val fields: Map<ServiceField, String> = emptyMap(),
    val reminderEnabled: Boolean = false,
    val errors: Map<ServiceField, Int> = emptyMap(),
    val isSaving: Boolean = false,
    @param:StringRes val submissionError: Int? = null,
) {
    operator fun get(field: ServiceField): String = fields[field].orEmpty()
}
