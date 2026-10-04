package studio.appvero.bikecare.features.maintenance.domain.model

import java.time.LocalDate

/** A scheduled task, not a completed service. Dates are calendar dates, independent of timezone. */
data class MaintenanceItem(
    val id: String,
    val bikeId: String,
    val name: String,
    val dueDate: LocalDate,
    val dueOdometerKm: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class MaintenanceStatus { OVERDUE, DUE_SOON, UP_TO_DATE }

data class MaintenanceAssessment(
    val item: MaintenanceItem,
    val status: MaintenanceStatus,
    val remainingDays: Long,
    val remainingKm: Long,
    /** 0 at/outside the warning window, 1 when either due threshold is reached. */
    val dueWindowProgress: Float,
)
