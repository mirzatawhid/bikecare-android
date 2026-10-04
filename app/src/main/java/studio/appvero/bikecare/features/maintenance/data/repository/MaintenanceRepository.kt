package studio.appvero.bikecare.features.maintenance.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem

interface MaintenanceRepository {
    fun observeMaintenance(bikeId: String): Flow<List<MaintenanceItem>>
}

enum class MaintenanceFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, Unknown }
class MaintenanceException(val failure: MaintenanceFailure) : Exception(failure.name)
