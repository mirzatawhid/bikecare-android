package studio.appvero.bikecare.features.maintenance.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem
import studio.appvero.bikecare.features.maintenance.domain.model.ServiceLog

interface MaintenanceRepository {
    fun observeMaintenance(bikeId: String): Flow<List<MaintenanceItem>>
    suspend fun logService(log: ServiceLog)
}

enum class MaintenanceFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, Unknown }
class MaintenanceException(val failure: MaintenanceFailure) : Exception(failure.name)
