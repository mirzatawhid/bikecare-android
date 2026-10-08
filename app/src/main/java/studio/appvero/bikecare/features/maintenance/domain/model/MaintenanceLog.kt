package studio.appvero.bikecare.features.maintenance.domain.model

/** Dates are epoch milliseconds. Cost is a whole BDT amount. */
data class MaintenanceLog(
    val id: String,
    val bikeId: String,
    val title: String,
    val category: MaintenanceCategory,
    val serviceType: MaintenanceServiceType,
    val date: Long,
    val odometer: Long,
    val cost: Long,
    val provider: String,
    val description: String,
    val nextServiceOdometer: Long?,
    val nextServiceDate: Long?,
    val receiptUrl: String? = null,
    val createdAt: Long = 0,
    val isSyncPending: Boolean = false,
)

enum class MaintenanceCategory { ENGINE, BRAKES, TIRES, ELECTRICAL, GENERAL }
enum class MaintenanceServiceType { PREVENTIVE, REPAIR, INSPECTION }
