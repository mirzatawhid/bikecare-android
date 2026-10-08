package studio.appvero.bikecare.features.garage.domain.model

/** Timestamps are epoch milliseconds; Firestore timestamps stay in the data layer. */
data class Bike(
    val id: String,
    val brand: String,
    val model: String,
    val year: Int,
    val registrationNumber: String,
    val initialOdometer: Long,
    val currentOdometer: Long,
    val imageUrl: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isActive: Boolean = true,
    /** Transient Firestore snapshot metadata; this is not stored in the bike document. */
    val isSyncPending: Boolean = false,
)
