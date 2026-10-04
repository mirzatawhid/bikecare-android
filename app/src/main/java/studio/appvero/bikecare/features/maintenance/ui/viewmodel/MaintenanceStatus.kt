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
    val days = ChronoUnit.DAYS.between(today, dueDate)
    val km = dueOdometerKm - currentOdometerKm
    val status = when {
        days <= 0 || km <= 0 -> MaintenanceStatus.OVERDUE
        days <= DUE_SOON_DAYS || km <= DUE_SOON_KM -> MaintenanceStatus.DUE_SOON
        else -> MaintenanceStatus.UP_TO_DATE
    }
    val proximity = minOf(days.toDouble() / DUE_SOON_DAYS, km.toDouble() / DUE_SOON_KM)
    return MaintenanceAssessment(this, status, days, km, (1.0 - proximity).coerceIn(0.0, 1.0).toFloat())
}

/** Compare date and distance using warning windows; stable ID breaks identical ties. */
internal fun List<MaintenanceAssessment>.byPriority(): List<MaintenanceAssessment> = sortedWith(
    compareBy<MaintenanceAssessment> { it.status.ordinal }
        .thenBy { minOf(it.remainingDays.toDouble() / DUE_SOON_DAYS, it.remainingKm.toDouble() / DUE_SOON_KM) }
        .thenBy { it.item.dueDate }
        .thenBy { it.item.dueOdometerKm }
        .thenBy { it.item.id },
)
