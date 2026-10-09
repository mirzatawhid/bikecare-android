package studio.appvero.bikecare.features.fuel.domain.model

/** Dates are epoch milliseconds. Quantity is liters and prices are BDT. */
data class FuelLog(
    val id: String,
    val bikeId: String,
    val date: Long,
    val odometer: Long,
    val fuelType: String,
    val quantity: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val station: String,
    val fullTank: Boolean,
    val notes: String,
    val createdAt: Long = 0,
    val isSyncPending: Boolean = false,
)
