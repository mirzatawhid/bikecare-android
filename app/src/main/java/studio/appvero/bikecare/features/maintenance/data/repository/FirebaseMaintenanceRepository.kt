package studio.appvero.bikecare.features.maintenance.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
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
                                trySend(snapshot.documents.map { it.toMaintenance(bikeId) })
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
        val dueDate = try {
            LocalDate.parse(getString("dueDate") ?: invalid())
        } catch (_: java.time.DateTimeException) {
            invalid()
        }
        val odometer = getLong("dueOdometerKm") ?: invalid()
        val created = getTimestamp("createdAt")?.toDate()?.time ?: invalid()
        val updated = getTimestamp("updatedAt")?.toDate()?.time ?: invalid()
        if (getString("id") != id || getString("bikeId") != bikeId ||
            name.isBlank() || name.length > 100 || odometer !in 0..10_000_000 ||
            updated < created || dueDate.year !in 1900..9999
        ) invalid()
        return MaintenanceItem(id, bikeId, name, dueDate, odometer, created, updated)
    }

    private fun mapError(error: Exception): MaintenanceException {
        if (error is MaintenanceException) return error
        return MaintenanceException(when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> MaintenanceFailure.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> MaintenanceFailure.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> MaintenanceFailure.AuthenticationExpired
            else -> MaintenanceFailure.Unknown
        })
    }
}
