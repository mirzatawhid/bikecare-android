package studio.appvero.bikecare.features.maintenance.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceCategory
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceLog
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceServiceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMaintenanceRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : MaintenanceRepository {
    private fun logs(uid: String) = firestore.collection("users").document(uid).collection("maintenanceLogs")

    private fun requireUid(): String {
        val user = auth.currentUser ?: throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
        if (!user.isEmailVerified) throw MaintenanceException(MaintenanceFailure.PermissionDenied)
        return user.uid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentLogs(): Flow<List<MaintenanceLog>> = authRepository.observeAuthState()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            if (user == null || !user.isEmailVerified) flow {
                emit(emptyList())
                throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
            } else callbackFlow {
                val registration = logs(user.uid).orderBy("date", Query.Direction.DESCENDING).limit(100)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (auth.currentUser?.uid != user.uid) {
                            trySend(emptyList())
                            close(MaintenanceException(MaintenanceFailure.AuthenticationExpired))
                        } else if (error != null) {
                            close(mapError(error))
                        } else if (snapshot != null) {
                            try {
                                trySend(snapshot.documents.map { it.toLog() })
                            } catch (error: Exception) {
                                close(mapError(error))
                            }
                        }
                    }
                awaitClose { registration.remove() }
            }
        }

    override suspend fun addLog(log: MaintenanceLog) {
        try {
            validate(log)
            val uid = requireUid()
            val reference = logs(uid).document(log.id)
            if (requireUid() != uid) throw MaintenanceException(MaintenanceFailure.AuthenticationExpired)
            reference.set(mapOf(
                "bikeId" to log.bikeId,
                "title" to log.title.trim(),
                "category" to log.category.name,
                "serviceType" to log.serviceType.name,
                "date" to Timestamp(java.util.Date(log.date)),
                "odometer" to log.odometer,
                "cost" to log.cost,
                "provider" to log.provider.trim(),
                "description" to log.description.trim(),
                "nextServiceOdometer" to log.nextServiceOdometer,
                "nextServiceDate" to log.nextServiceDate?.let { Timestamp(java.util.Date(it)) },
                "receiptUrl" to null,
                "createdAt" to FieldValue.serverTimestamp(),
            ))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw mapError(error)
        }
    }

    private fun DocumentSnapshot.toLog(): MaintenanceLog = try {
        MaintenanceLog(
            id = id,
            bikeId = requireNotNull(getString("bikeId")),
            title = requireNotNull(getString("title")),
            category = MaintenanceCategory.valueOf(requireNotNull(getString("category"))),
            serviceType = MaintenanceServiceType.valueOf(requireNotNull(getString("serviceType"))),
            date = requireNotNull(getTimestamp("date")).toDate().time,
            odometer = requireNotNull(getLong("odometer")),
            cost = requireNotNull(getLong("cost")),
            provider = requireNotNull(getString("provider")),
            description = requireNotNull(getString("description")),
            nextServiceOdometer = getLong("nextServiceOdometer"),
            nextServiceDate = getTimestamp("nextServiceDate")?.toDate()?.time,
            receiptUrl = getString("receiptUrl"),
            createdAt = requireNotNull(getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)).toDate().time,
            isSyncPending = metadata.hasPendingWrites(),
        ).also(::validate)
    } catch (error: Exception) {
        throw mapError(error)
    }

    private fun validate(log: MaintenanceLog) {
        if (log.id.isBlank() || log.id.length > 128 || '/' in log.id || log.bikeId.isBlank() ||
            log.bikeId.length > 128 || '/' in log.bikeId || log.title.trim().length !in 1..100 ||
            log.date !in 0..4102444800000L || log.odometer !in 0..10_000_000 ||
            log.cost !in 0..1_000_000_000 || log.provider.trim().length > 100 ||
            log.description.trim().length > 1000 ||
            (log.nextServiceOdometer != null && log.nextServiceOdometer !in log.odometer..10_000_000) ||
            (log.nextServiceDate != null && log.nextServiceDate !in log.date..4102444800000L) ||
            log.receiptUrl != null
        ) throw MaintenanceException(MaintenanceFailure.InvalidData)
    }

    private fun mapError(error: Exception): MaintenanceException {
        if (error is MaintenanceException) return error
        return MaintenanceException(when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> MaintenanceFailure.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> MaintenanceFailure.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> MaintenanceFailure.AuthenticationExpired
            else -> MaintenanceFailure.Unknown
        })
    }
}
