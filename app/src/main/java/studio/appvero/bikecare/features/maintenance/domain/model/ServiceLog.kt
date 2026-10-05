package studio.appvero.bikecare.features.maintenance.domain.model

import java.time.LocalDate

/** Immutable completed service; money is stored as integer poisha, never floating point. */
data class ServiceLog(
    val id: String,
    val bikeId: String,
    val maintenanceId: String?,
    val name: String,
    val serviceDate: LocalDate,
    val odometerKm: Long,
    val totalCostPoisha: Long?,
    val workshop: String,
    val notes: String,
    val reminder: ServiceReminder?,
)

data class ServiceReminder(
    val dueDate: LocalDate?,
    val dueOdometerKm: Long?,
    val repeatEveryKm: Long?,
    val repeatEveryDays: Long?,
)
