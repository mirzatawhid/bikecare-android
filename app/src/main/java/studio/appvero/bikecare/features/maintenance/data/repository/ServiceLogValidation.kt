package studio.appvero.bikecare.features.maintenance.data.repository

import studio.appvero.bikecare.features.maintenance.domain.model.ServiceLog

internal fun serviceOdometer(current: Long, logged: Long): Long = maxOf(current, logged)

internal fun validateServiceLog(log: ServiceLog) {
    fun validId(id: String) = id.isNotBlank() && id.length <= 128 && '/' !in id && id != "." && id != ".."
    val reminder = log.reminder
    if (!validId(log.id) || !validId(log.bikeId) || log.maintenanceId?.let { !validId(it) } == true ||
        log.name.trim().length !in 1..100 || log.serviceDate.year !in 1900..9999 ||
        log.odometerKm !in 0..10_000_000 || log.totalCostPoisha?.let { it !in 0..100_000_000_000L } == true ||
        log.workshop.length > 200 || log.notes.length > 2000 ||
        reminder?.let { (it.dueDate == null && it.dueOdometerKm == null) ||
            it.dueDate?.year?.let { year -> year !in 1900..9999 } == true ||
            it.dueOdometerKm?.let { km -> km !in 0..10_000_000 } == true ||
            it.repeatEveryKm?.let { km -> km !in 1..10_000_000 } == true ||
            it.repeatEveryDays?.let { days -> days !in 1..10_000_000 } == true } == true
    ) throw MaintenanceException(MaintenanceFailure.InvalidData)
}
