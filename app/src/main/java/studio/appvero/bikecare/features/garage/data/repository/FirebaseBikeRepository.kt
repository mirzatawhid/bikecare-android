package studio.appvero.bikecare.features.garage.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.garage.domain.model.Bike
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseBikeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : BikeRepository {
    private fun requireUid(): String {
        val user = auth.currentUser ?: throw BikeException(BikeFailure.AuthenticationExpired)
        Log.d("BikeRepository", "mapError: ${user.isEmailVerified}")
        if (!user.isEmailVerified) throw BikeException(BikeFailure.PermissionDenied)
        return user.uid
    }

    private fun bikes(uid: String) = firestore.collection("users").document(uid).collection("bikes")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUserBikes(): Flow<List<Bike>> = authRepository.observeAuthState()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            if (user == null || !user.isEmailVerified) flow {
                emit(emptyList())
                throw BikeException(BikeFailure.AuthenticationExpired)
            } else callbackFlow {
                val registration = bikes(user.uid).orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (auth.currentUser?.uid != user.uid) {
                            trySend(emptyList())
                            close(BikeException(BikeFailure.AuthenticationExpired))
                        } else if (error != null) {
                            close(mapError(error))
                        } else if (snapshot != null) {
                            try {
                                trySend(snapshot.documents.map { it.toBike() })
                            } catch (error: Exception) {
                                close(mapError(error))
                            }
                        }
                    }
                awaitClose { registration.remove() }
            }
        }

    override suspend fun addBike(bike: Bike) = write(bike, creating = true)
    override suspend fun updateBike(bike: Bike) = write(bike, creating = false)

    private suspend fun write(bike: Bike, creating: Boolean): Unit = mapped {
        validate(bike)
        val uid = requireUid()
        val reference = bikes(uid).document(bike.id)
        if (requireUid() != uid) throw BikeException(BikeFailure.AuthenticationExpired)
        val fields = mutableMapOf(
            "id" to bike.id,
            "brand" to bike.brand.trim(),
            "model" to bike.model.trim(),
            "year" to bike.year,
            "registrationNumber" to bike.registrationNumber.trim(),
            "initialOdometer" to bike.initialOdometer,
            "currentOdometer" to bike.currentOdometer,
            "imageUrl" to bike.imageUrl,
            "isActive" to bike.isActive,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (creating) {
            fields["createdAt"] = FieldValue.serverTimestamp()
            reference.set(fields)
        } else {
            fields.remove("initialOdometer")
            reference.update(fields)
        }
    }

    override suspend fun deleteBike(id: String): Unit = mapped {
        validateId(id)
        val uid = requireUid()
        val reference = bikes(uid).document(id)
        if (requireUid() != uid) throw BikeException(BikeFailure.AuthenticationExpired)
        reference.delete()
    }

    private fun validateId(id: String) {
        if (id.isBlank() || id.length > 128 || '/' in id || id == "." || id == "..") {
            throw BikeException(BikeFailure.InvalidData)
        }
    }

    private fun validate(bike: Bike) {
        validateId(bike.id)
        if (bike.brand.trim().length !in 1..100 || bike.model.trim().length !in 1..100 ||
            bike.year !in 1885..2100 || bike.registrationNumber.trim().length > 50 ||
            bike.initialOdometer < 0 || bike.currentOdometer < bike.initialOdometer ||
            bike.currentOdometer > 10_000_000 ||
            (bike.imageUrl != null && (!bike.imageUrl.startsWith("https://") || bike.imageUrl.length > 2048))
        ) {
            throw BikeException(BikeFailure.InvalidData)
        }
    }

    private fun DocumentSnapshot.toBike(): Bike {
        val bike = Bike(
            id = id,
            brand = getString("brand") ?: throw BikeException(BikeFailure.InvalidData),
            model = getString("model") ?: throw BikeException(BikeFailure.InvalidData),
            year = (getLong("year") ?: throw BikeException(BikeFailure.InvalidData)).also {
                if (it !in 1885L..2100L) throw BikeException(BikeFailure.InvalidData)
            }.toInt(),
            registrationNumber = getString("registrationNumber")
                ?: throw BikeException(BikeFailure.InvalidData),
            initialOdometer = getLong("initialOdometer")
                ?: throw BikeException(BikeFailure.InvalidData),
            currentOdometer = getLong("currentOdometer")
                ?: throw BikeException(BikeFailure.InvalidData),
            imageUrl = getString("imageUrl"),
            // Pending server timestamps are unresolved in the local snapshot; use Firestore's
            // estimate so queued offline writes can still be rendered in the Garage.
            createdAt = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
                ?: throw BikeException(BikeFailure.InvalidData),
            updatedAt = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
                ?: throw BikeException(BikeFailure.InvalidData),
            isActive = getBoolean("isActive") ?: throw BikeException(BikeFailure.InvalidData),
            isSyncPending = metadata.hasPendingWrites(),
        )
        if (getString("id") != id) throw BikeException(BikeFailure.InvalidData)
        validate(bike)
        return bike
    }

    private suspend fun <T> mapped(block: suspend () -> T): T = try {
        block()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        throw mapError(error)
    }

    private fun mapError(error: Exception): BikeException {
        if (error is BikeException) return error
        if (error.cause is BikeException) return error.cause as BikeException
        Log.d("BikeRepository", "mapError: $error")
        return BikeException(
            when ((error as? FirebaseFirestoreException)?.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> BikeFailure.Network
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> BikeFailure.PermissionDenied
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> BikeFailure.AuthenticationExpired
                else -> BikeFailure.Unknown
            }
        )
    }
}
