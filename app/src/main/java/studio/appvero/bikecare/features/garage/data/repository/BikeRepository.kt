package studio.appvero.bikecare.features.garage.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.garage.domain.model.Bike

interface BikeRepository {
    /** Caller supplies a stable, nonempty ID. Writes are queued locally and may sync when online. */
    suspend fun addBike(bike: Bike)
    fun observeUserBikes(): Flow<List<Bike>>
    suspend fun updateBike(bike: Bike)
    suspend fun deleteBike(id: String)
}

enum class BikeFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, NotFound, AlreadyExists, Unknown }
class BikeException(val failure: BikeFailure) : Exception(failure.name)
