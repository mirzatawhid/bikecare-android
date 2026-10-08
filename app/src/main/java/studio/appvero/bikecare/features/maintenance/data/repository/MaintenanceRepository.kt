package studio.appvero.bikecare.features.maintenance.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceLog

interface MaintenanceRepository {
    /** Emits the 100 most recent logs. A successful call queues a local write; server acceptance is asynchronous. */
    fun observeRecentLogs(): Flow<List<MaintenanceLog>>
    suspend fun addLog(log: MaintenanceLog)
}

enum class MaintenanceFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, Unknown }
class MaintenanceException(val failure: MaintenanceFailure) : Exception(failure.name)
