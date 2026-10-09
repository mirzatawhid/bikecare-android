package studio.appvero.bikecare.features.fuel.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.fuel.domain.model.FuelLog

interface FuelLogRepository {
    /** Emits the 100 most recent logs. Local writes may still be rejected by the server. */
    fun observeRecentLogs(): Flow<List<FuelLog>>
    suspend fun addLog(log: FuelLog)
}

enum class FuelLogFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, Unknown }
class FuelLogException(val failure: FuelLogFailure) : Exception(failure.name)
