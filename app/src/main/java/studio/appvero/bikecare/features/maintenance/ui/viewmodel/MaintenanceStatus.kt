package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceAssessment
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Pure helpers alongside the ViewModel, following the existing AuthValidation pattern.
internal const val DUE_SOON_DAYS = 30L
internal const val DUE_SOON_KM = 1_000L

internal fun MaintenanceItem.assess(today: LocalDate, currentOdometerKm: Long): MaintenanceAssessment {
    val days = dueDate?.let { ChronoUnit.DAYS.between(today, it) }
    val km = dueOdometerKm?.minus(currentOdometerKm)
    val thresholds = buildList {
        days?.let { add(it to DUE_SOON_DAYS) }
        km?.let { add(it to DUE_SOON_KM) }
    }
    val status = when {
        thresholds.any { (remaining, _) -> remaining <= 0 } -> MaintenanceStatus.OVERDUE
        thresholds.any { (remaining, warningWindow) -> remaining <= warningWindow } -> MaintenanceStatus.DUE_SOON
        else -> MaintenanceStatus.UP_TO_DATE
    }
    val proximity = thresholds.minOfOrNull { (remaining, warningWindow) -> remaining.toDouble() / warningWindow } ?: 1.0
    return MaintenanceAssessment(this, status, days, km, (1.0 - proximity).coerceIn(0.0, 1.0).toFloat())
}

/** Compare date and distance using warning windows; stable ID breaks identical ties. */
internal fun List<MaintenanceAssessment>.byPriority(): List<MaintenanceAssessment> = sortedWith(
    compareBy<MaintenanceAssessment> { it.status.ordinal }
        .thenBy { assessment ->
            listOfNotNull(
                assessment.remainingDays?.toDouble()?.div(DUE_SOON_DAYS),
                assessment.remainingKm?.toDouble()?.div(DUE_SOON_KM),
            ).minOrNull() ?: 1.0
        }
        .thenBy { it.item.dueDate }
        .thenBy { it.item.dueOdometerKm }
        .thenBy { it.item.id },
)
