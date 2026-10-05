package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.maintenance.domain.model.ServiceLog
import studio.appvero.bikecare.features.maintenance.domain.model.ServiceReminder
import studio.appvero.bikecare.features.maintenance.ui.screen.LogServiceFormState
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField.*
import java.time.LocalDate

internal data class ServiceValidation(val errors: Map<ServiceField, Int>, val log: ServiceLog?)

internal fun LogServiceFormState.validateService(): ServiceValidation {
    val errors = mutableMapOf<ServiceField, Int>()
    fun invalid(field: ServiceField, message: Int = R.string.service_invalid_number) { errors[field] = message }
    fun date(field: ServiceField): LocalDate? = runCatching { LocalDate.parse(this[field].trim()) }
        .getOrNull()?.takeIf { it.year in 1900..9999 }
    fun number(field: ServiceField, required: Boolean = false, positive: Boolean = false): Long? {
        val text = this[field].trim()
        if (text.isEmpty() && !required) return null
        return text.toLongOrNull()?.takeIf { it in (if (positive) 1L else 0L)..10_000_000L }
            .also { if (it == null) invalid(field) }
    }
    if (this[Name].trim().length !in 1..100) invalid(Name, R.string.service_name_error)
    val serviceDate = date(ServiceDate)
    if (serviceDate == null) invalid(ServiceDate, R.string.service_date_error)
    val odometer = number(Odometer, required = true)
    val costText = this[Cost].trim()
    val cost = if (costText.isEmpty()) null else runCatching {
        costText.toBigDecimal().movePointRight(2).longValueExact()
    }.getOrNull()?.takeIf { it in 0..100_000_000_000L }
    if (costText.isNotEmpty() && cost == null) invalid(Cost, R.string.service_cost_error)
    if (this[Workshop].trim().length > 200) invalid(Workshop, R.string.service_text_error)
    if (this[Notes].trim().length > 2000) invalid(Notes, R.string.service_text_error)
    var reminder: ServiceReminder? = null
    if (reminderEnabled) {
        val repeatKm = number(RepeatKm, positive = true)
        val repeatDays = number(RepeatDays, positive = true)
        val dueKmText = this[DueOdometer].trim()
        val manualKm = number(DueOdometer)
        val calculatedKm = if (dueKmText.isEmpty() && odometer != null && repeatKm != null) {
            runCatching { Math.addExact(odometer, repeatKm) }.getOrNull()
        } else null
        val dueKm = if (dueKmText.isNotEmpty()) manualKm else
            calculatedKm
        if (dueKmText.isNotEmpty() && (manualKm == null || manualKm !in 0..10_000_000) ||
            dueKm != null && dueKm !in 0..10_000_000 ||
            dueKmText.isEmpty() && repeatKm != null && odometer != null &&
                (calculatedKm == null || calculatedKm !in 0..10_000_000)) {
            invalid(DueOdometer, R.string.service_due_km_error)
        }

        val dueDateText = this[DueDate].trim()
        val manualDate = if (dueDateText.isNotEmpty()) date(DueDate) else null
        val calculatedDate = if (dueDateText.isEmpty() && serviceDate != null && repeatDays != null) {
            runCatching { serviceDate.plusDays(repeatDays) }.getOrNull()
        } else null
        val dueDate = if (dueDateText.isNotEmpty()) manualDate else
            calculatedDate
        if (dueDateText.isNotEmpty() && manualDate == null || dueDate != null && dueDate.year !in 1900..9999 ||
            dueDateText.isEmpty() && repeatDays != null && serviceDate != null &&
                (calculatedDate == null || calculatedDate.year !in 1900..9999)) {
            invalid(DueDate, R.string.service_due_date_error)
        }

        if (dueKm == null && dueDate == null && errors[DueOdometer] == null && errors[DueDate] == null) {
            invalid(ReminderDue, R.string.service_due_required)
        }
        if ((dueKm != null || dueDate != null) && errors[DueOdometer] == null && errors[DueDate] == null) {
            reminder = ServiceReminder(dueDate, dueKm, repeatKm, repeatDays)
        }
    }
    return ServiceValidation(errors, if (errors.isNotEmpty()) null else ServiceLog(
        id, bike.id, maintenanceId, this[Name].trim(), requireNotNull(serviceDate), requireNotNull(odometer),
        cost, this[Workshop].trim(), this[Notes].trim(), reminder,
    ))
}
