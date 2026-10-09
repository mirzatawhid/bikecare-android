package studio.appvero.bikecare.features.fuel.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.fuel.domain.model.FuelLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class FirebaseFuelLogRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : FuelLogRepository {
    private fun logs(uid: String) = firestore.collection("users").document(uid).collection("fuelLogs")

    private fun requireUid(): String {
        val user = auth.currentUser ?: throw FuelLogException(FuelLogFailure.AuthenticationExpired)
        if (!user.isEmailVerified) throw FuelLogException(FuelLogFailure.PermissionDenied)
        return user.uid
    }

    private companion object {
        const val TAG = "FirebaseFuelLogRepository"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentLogs(): Flow<List<FuelLog>> =
        authRepository.observeAuthState()
            .distinctUntilChanged()
            .flatMapLatest { user ->

                Log.d(
                    TAG,
                    "observeRecentLogs: Auth state changed, " +
                            "uid=${user?.uid}, verified=${user?.isEmailVerified}"
                )

                if (user == null || !user.isEmailVerified) {
                    flow {
                        Log.w(
                            TAG,
                            "observeRecentLogs: User not authenticated or email not verified"
                        )

                        emit(emptyList())

                        throw FuelLogException(
                            FuelLogFailure.AuthenticationExpired
                        )
                    }
                } else {
                    callbackFlow {
                        val uid = user.uid

                        val query = logs(uid)
                            .orderBy("date", Query.Direction.DESCENDING)
                            .limit(100)

                        Log.d(
                            TAG,
                            "observeRecentLogs: Registering Firestore listener, " +
                                    "path=${logs(uid).path}"
                        )

                        val registration = query.addSnapshotListener(
                            MetadataChanges.INCLUDE
                        ) { snapshot, error ->

                            Log.d(
                                TAG,
                                "observeRecentLogs: Snapshot callback triggered"
                            )

                            if (auth.currentUser?.uid != uid) {
                                Log.w(
                                    TAG,
                                    "observeRecentLogs: Authentication changed, " +
                                            "expectedUid=$uid, " +
                                            "currentUid=${auth.currentUser?.uid}"
                                )

                                trySend(emptyList())

                                close(
                                    FuelLogException(
                                        FuelLogFailure.AuthenticationExpired
                                    )
                                )
                            } else if (error != null) {
                                Log.e(
                                    TAG,
                                    "observeRecentLogs: Firestore listener failed, " +
                                            "code=${error.code}, message=${error.message}",
                                    error
                                )

                                close(mapError(error))
                            } else if (snapshot != null) {
                                Log.d(
                                    TAG,
                                    "observeRecentLogs: Snapshot received, " +
                                            "documents=${snapshot.size()}, " +
                                            "fromCache=${snapshot.metadata.isFromCache}, " +
                                            "pendingWrites=${snapshot.metadata.hasPendingWrites()}"
                                )

                                try {
                                    val fuelLogs = snapshot.documents.map { document ->
                                        Log.d(
                                            TAG,
                                            "observeRecentLogs: Mapping document, " +
                                                    "id=${document.id}"
                                        )

                                        document.toLog()
                                    }

                                    Log.d(
                                        TAG,
                                        "observeRecentLogs: Successfully mapped " +
                                                "${fuelLogs.size} fuel logs"
                                    )

                                    val result = trySend(fuelLogs)

                                    if (result.isFailure) {
                                        Log.w(
                                            TAG,
                                            "observeRecentLogs: Failed to emit fuel logs, " +
                                                    "channel may be closed"
                                        )
                                    } else {
                                        Log.d(
                                            TAG,
                                            "observeRecentLogs: Emitted ${fuelLogs.size} fuel logs"
                                        )
                                    }

                                } catch (error: Exception) {
                                    Log.e(
                                        TAG,
                                        "observeRecentLogs: Document mapping failed, " +
                                                "message=${error.message}",
                                        error
                                    )

                                    close(mapError(error))
                                }
                            } else {
                                Log.w(
                                    TAG,
                                    "observeRecentLogs: Received null snapshot without error"
                                )
                            }
                        }

                        awaitClose {
                            Log.d(
                                TAG,
                                "observeRecentLogs: Removing Firestore listener, uid=$uid"
                            )

                            registration.remove()
                        }
                    }
                }
            }

    override suspend fun addLog(log: FuelLog) {
        try {
            Log.d("FuelLogRepository", "addLog started: logId=${log.id}, bikeId=${log.bikeId}")

            validate(log)
            Log.d("FuelLogRepository", "Validation passed for logId=${log.id}")

            val uid = requireUid()
            Log.d("FuelLogRepository", "Authenticated user uid=$uid")

            val reference = logs(uid).document(log.id)
            Log.d(
                "FuelLogRepository",
                "Firestore path=${reference.path}",
            )

            if (requireUid() != uid) {
                Log.d("FuelLogRepository", "Authentication changed before write")
                throw FuelLogException(FuelLogFailure.AuthenticationExpired)
            }

            Log.d(
                "FuelLogRepository",
                "Writing fuel log: odometer=${log.odometer}, quantity=${log.quantity}, totalCost=${log.totalCost}",
            )

            val user = auth.currentUser
                ?: throw FuelLogException(
                    FuelLogFailure.AuthenticationExpired
                )

            user.reload().await()

            val token = user.getIdToken(true).await()

            Log.d(TAG, "Firebase project=${firestore.app.options.projectId}")
            Log.d(TAG, "Auth UID=${user.uid}")
            Log.d(TAG, "Email verified=${auth.currentUser?.isEmailVerified}")
            Log.d(TAG, "Token refreshed=${token.token != null}")

            val bikeRef = firestore
                .collection("users")
                .document(user.uid)
                .collection("bikes")
                .document(log.bikeId)

            try {
                val bikeSnapshot = bikeRef.get(Source.SERVER).await()

                Log.d(TAG, "Bike path=${bikeRef.path}")
                Log.d(TAG, "Bike exists=${bikeSnapshot.exists()}")
            } catch (e: Exception) {
                Log.e(TAG, "Bike lookup failed", e)
            }

            try {
                val existingLog = reference.get(Source.SERVER).await()

                Log.d(TAG, "Fuel log exists=${existingLog.exists()}")
                Log.d(TAG, "Fuel log path=${reference.path}")
            } catch (e: Exception) {
                Log.e(TAG, "Fuel log lookup failed", e)
            }

            reference.set(
                mapOf(
                    "bikeId" to log.bikeId,
                    "date" to Timestamp(java.util.Date(log.date)),
                    "odometer" to log.odometer,
                    "fuelType" to log.fuelType.trim(),
                    "quantity" to log.quantity,
                    "pricePerLiter" to log.pricePerLiter,
                    "totalCost" to log.totalCost,
                    "station" to log.station.trim(),
                    "fullTank" to log.fullTank,
                    "notes" to log.notes.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

            Log.d(
                "FuelLogRepository",
                "Fuel log successfully added: logId=${log.id}",
            )
        } catch (cancelled: CancellationException) {
            Log.d("FuelLogRepository", "addLog cancelled: logId=${log.id}")
            throw cancelled
        } catch (error: Exception) {
            Log.d(
                "FuelLogRepository",
                "addLog failed: logId=${log.id}, error=${error.message}",
                error,
            )
            throw mapError(error)
        }
    }

    private fun DocumentSnapshot.toLog(): FuelLog = try {
        FuelLog(
            id = id,
            bikeId = requireNotNull(getString("bikeId")),
            date = requireNotNull(getTimestamp("date")).toDate().time,
            odometer = requireNotNull(getLong("odometer")),
            fuelType = requireNotNull(getString("fuelType")),
            quantity = requireNotNull(getDouble("quantity")),
            pricePerLiter = requireNotNull(getDouble("pricePerLiter")),
            totalCost = requireNotNull(getDouble("totalCost")),
            station = requireNotNull(getString("station")),
            fullTank = requireNotNull(getBoolean("fullTank")),
            notes = requireNotNull(getString("notes")),
            createdAt = requireNotNull(getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)).toDate().time,
            isSyncPending = metadata.hasPendingWrites(),
        ).also(::validate)
    } catch (error: Exception) {
        throw mapError(error)
    }

    private fun validate(log: FuelLog) {
        if (log.id.isBlank() || log.id.length > 128 || '/' in log.id ||
            log.bikeId.isBlank() || log.bikeId.length > 128 || '/' in log.bikeId ||
            log.date !in 0..4102444800000L || log.odometer !in 0..10_000_000 ||
            log.fuelType.trim().length !in 1..50 ||
            !log.quantity.isFinite() || log.quantity <= 0 || log.quantity > 1000 ||
            !log.pricePerLiter.isFinite() || log.pricePerLiter <= 0 || log.pricePerLiter > 100_000 ||
            !log.totalCost.isFinite() || log.totalCost <= 0 || log.totalCost > 100_000_000 ||
            abs(log.totalCost - log.quantity * log.pricePerLiter) > 0.011 ||
            log.station.trim().length > 100 || log.notes.trim().length > 1000
        ) throw FuelLogException(FuelLogFailure.InvalidData)
    }

    private fun mapError(error: Exception): FuelLogException {
        if (error is FuelLogException) return error
        return FuelLogException(when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> FuelLogFailure.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FuelLogFailure.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> FuelLogFailure.AuthenticationExpired
            else -> FuelLogFailure.Unknown
        })
    }
}
