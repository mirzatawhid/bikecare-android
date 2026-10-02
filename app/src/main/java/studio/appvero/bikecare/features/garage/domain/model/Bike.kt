package studio.appvero.bikecare.features.garage.domain.model

/** Timestamps are epoch milliseconds; Firestore timestamps stay in the data layer. */
data class Bike(
    val id: String,
    val brand: String,
    val model: String,
    val nickname: String? = null,
    val year: Int,
    val engineCapacityCc: Int? = null,
    val registrationNumber: String,
    val initialOdometer: Long,
    val currentOdometer: Long,
    val imageUrl: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isActive: Boolean = true,
)
