package studio.appvero.bikecare.features.maintenance.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem
import studio.appvero.bikecare.features.maintenance.domain.model.ServiceLog
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMaintenanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : MaintenanceRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMaintenance(bikeId: String): Flow<List<MaintenanceItem>> =
        authRepository.observeAuthState().distinctUntilChanged().flatMapLatest { user ->
            if (user == null || !user.isEmailVerified) flow {
                emit(emptyList())
                throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
            } else callbackFlow {
                // Match Garage: initial cache alone cannot establish an authoritative empty list.
                val initialTimeout = launch {
                    delay(15_000)
                    close(MaintenanceException(MaintenanceFailure.Network))
                }
                val listener = firestore.collection("users").document(user.uid)
                    .collection("maintenanceLogs").whereEqualTo("bikeId", bikeId)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        val currentUser = authRepository.getCurrentUser()
                        if (currentUser?.uid != user.uid || !currentUser.isEmailVerified) {
                            trySend(emptyList())
                            close(MaintenanceException(MaintenanceFailure.AuthenticationExpired))
                        } else if (error != null) {
                            close(mapError(error))
                        } else if (snapshot != null && !snapshot.metadata.isFromCache) {
                            initialTimeout.cancel()
                            try {
                                trySend(snapshot.documents.map { it.toMaintenance(bikeId) }.filterNot { it.isCompleted })
                            } catch (error: Exception) {
                                close(mapError(error))
                            }
                        }
                    }
                awaitClose { initialTimeout.cancel(); listener.remove() }
            }
        }

    private fun DocumentSnapshot.toMaintenance(bikeId: String): MaintenanceItem {
        fun invalid(): Nothing = throw MaintenanceException(MaintenanceFailure.InvalidData)
        val name = getString("name") ?: invalid()
        val dueDateValue = get("dueDate")
        val dueDate = if (dueDateValue == null) null else try {
            LocalDate.parse(getString("dueDate") ?: invalid())
        } catch (_: java.time.DateTimeException) {
            invalid()
        }
        val odometerValue = get("dueOdometerKm")
        val odometer = if (odometerValue == null) null else getLong("dueOdometerKm") ?: invalid()
        val created = getTimestamp("createdAt")?.toDate()?.time ?: invalid()
        val updated = getTimestamp("updatedAt")?.toDate()?.time ?: invalid()
        if (getString("id") != id || getString("bikeId") != bikeId ||
            name.isBlank() || name.length > 100 ||
            dueDate == null && odometer == null || odometer?.let { it !in 0..10_000_000 } == true ||
            updated < created || dueDate?.year?.let { it !in 1900..9999 } == true
        ) invalid()
        val repeatKm = getLong("repeatEveryKm")
        val repeatDays = getLong("repeatEveryDays")
        if (repeatKm != null && repeatKm !in 1..10_000_000 ||
            repeatDays != null && repeatDays !in 1..10_000_000) invalid()
        return MaintenanceItem(id, bikeId, name, dueDate, odometer, created, updated,
            repeatKm, repeatDays, getBoolean("isCompleted") ?: false)
    }

    override suspend fun logService(log: ServiceLog) {
        try {
            validateServiceLog(log)
            val uid = requireUid()
            val user = firestore.collection("users").document(uid)
            val bikeRef = user.collection("bikes").document(log.bikeId)
            val logRef = user.collection("serviceLogs").document(log.id)
            // Stable IDs make retries safe, including a lost acknowledgement after commit.
            val taskId = log.maintenanceId ?: log.id.takeIf { log.reminder != null }
            val taskRef = taskId?.let { user.collection("maintenanceLogs").document(it) }
            firestore.runTransaction { transaction ->
                if (requireUid() != uid) throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
                val existingLog = transaction.get(logRef)
                val bike = transaction.get(bikeRef)
                if (!bike.exists()) throw MaintenanceException(MaintenanceFailure.InvalidData)
                val currentKm = bike.getLong("currentOdometer")
                    ?: throw MaintenanceException(MaintenanceFailure.InvalidData)
                val task = taskRef?.let { transaction.get(it) }
                if (existingLog.exists()) {
                    // Never silently accept edited input after an uncertain earlier result.
                    val sameService = existingLog.getString("bikeId") == log.bikeId &&
                        existingLog.getString("maintenanceId") == taskId && existingLog.getString("name") == log.name &&
                        existingLog.getString("serviceDate") == log.serviceDate.toString() &&
                        existingLog.getLong("odometerKm") == log.odometerKm &&
                        existingLog.getLong("totalCostPoisha") == log.totalCostPoisha &&
                        existingLog.getString("workshop") == log.workshop && existingLog.getString("notes") == log.notes
                    val sameReminder = log.reminder?.let {
                        task?.getBoolean("isCompleted") == false && task.getString("dueDate") == it.dueDate.toString() &&
                            task.getLong("dueOdometerKm") == it.dueOdometerKm &&
                            task.getLong("repeatEveryKm") == it.repeatEveryKm && task.getLong("repeatEveryDays") == it.repeatEveryDays
                    } ?: (task == null || task.getBoolean("isCompleted") == true)
                    if (!sameService || !sameReminder) throw MaintenanceException(MaintenanceFailure.InvalidData)
                    return@runTransaction Unit
                }
                if (log.maintenanceId == null && task?.exists() == true) throw MaintenanceException(MaintenanceFailure.InvalidData)
                if (log.maintenanceId != null && (task?.exists() != true || task.getString("bikeId") != log.bikeId)) {
                    throw MaintenanceException(MaintenanceFailure.InvalidData)
                }
                val timestamp = FieldValue.serverTimestamp()
                transaction.set(logRef, mapOf(
                    "id" to log.id, "bikeId" to log.bikeId, "maintenanceId" to taskId,
                    "name" to log.name, "serviceDate" to log.serviceDate.toString(),
                    "odometerKm" to log.odometerKm, "totalCostPoisha" to log.totalCostPoisha,
                    "workshop" to log.workshop, "notes" to log.notes,
                    "createdAt" to timestamp, "updatedAt" to timestamp,
                ))
                val nextKm = serviceOdometer(currentKm, log.odometerKm)
                if (nextKm != currentKm) transaction.update(bikeRef,
                    mapOf("currentOdometer" to nextKm, "updatedAt" to timestamp))
                if (taskRef != null) {
                    val reminder = log.reminder
                    if (reminder != null) {
                        transaction.set(taskRef, mapOf(
                            "id" to taskId, "bikeId" to log.bikeId, "name" to log.name,
                            "dueDate" to reminder.dueDate?.toString(), "dueOdometerKm" to reminder.dueOdometerKm,
                            "repeatEveryKm" to reminder.repeatEveryKm, "repeatEveryDays" to reminder.repeatEveryDays,
                            "isCompleted" to false, "createdAt" to (task?.getTimestamp("createdAt") ?: timestamp),
                            "updatedAt" to timestamp,
                        ))
                    } else transaction.update(taskRef, mapOf("isCompleted" to true, "updatedAt" to timestamp))
                }
                Unit
            }.await()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw mapError(error)
        }
    }

    private fun requireUid(): String {
        val user = authRepository.getCurrentUser()
        if (user == null || !user.isEmailVerified) throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
        return user.uid
    }

    private fun mapError(error: Exception): MaintenanceException {
        if (error is MaintenanceException) return error
        if (error.cause is MaintenanceException) return error.cause as MaintenanceException
        return MaintenanceException(when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> MaintenanceFailure.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> MaintenanceFailure.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> MaintenanceFailure.AuthenticationExpired
            else -> MaintenanceFailure.Unknown
        })
    }
}
