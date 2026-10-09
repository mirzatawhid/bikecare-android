package studio.appvero.bikecare.features.expense.data.repository

import com.google.firebase.Timestamp
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
import kotlinx.coroutines.tasks.await
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.expense.domain.model.Expense
import studio.appvero.bikecare.features.expense.domain.model.ExpenseCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseExpenseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) : ExpenseRepository {
    private fun expenses(uid: String) = firestore.collection("users").document(uid).collection("expenses")

    private fun requireUid(): String {
        val user = auth.currentUser ?: throw ExpenseException(ExpenseFailure.AuthenticationExpired)
        if (!user.isEmailVerified) throw ExpenseException(ExpenseFailure.PermissionDenied)
        return user.uid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentExpenses(): Flow<List<Expense>> = authRepository.observeAuthState()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            if (user == null || !user.isEmailVerified) flow {
                emit(emptyList())
                throw ExpenseException(ExpenseFailure.AuthenticationExpired)
            } else callbackFlow {
                val uid = user.uid
                val registration = expenses(uid).orderBy("date", Query.Direction.DESCENDING).limit(100)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (auth.currentUser?.uid != uid) {
                            trySend(emptyList())
                            close(ExpenseException(ExpenseFailure.AuthenticationExpired))
                        } else if (error != null) {
                            close(mapError(error))
                        } else if (snapshot != null) {
                            try {
                                trySend(snapshot.documents.map { it.toExpense() })
                            } catch (mappingError: Exception) {
                                close(mapError(mappingError))
                            }
                        }
                    }
                awaitClose { registration.remove() }
            }
        }

    override suspend fun addExpense(expense: Expense) {
        try {
            validate(expense)

            val uid = requireUid()

            val reference = expenses(uid)
                .document(expense.id)

            if (requireUid() != uid) {
                throw ExpenseException(
                    ExpenseFailure.AuthenticationExpired
                )
            }

            reference.set(
                mapOf(
                    "bikeId" to expense.bikeId,
                    "category" to expense.category.name,
                    "title" to expense.title.trim(),
                    "amount" to expense.amount,
                    "date" to Timestamp(
                        java.util.Date(expense.date)
                    ),
                    "notes" to expense.notes.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            ).await()

        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw mapError(error)
        }
    }

    private fun DocumentSnapshot.toExpense(): Expense = try {
        Expense(
            id = id,
            bikeId = requireNotNull(getString("bikeId")),
            category = ExpenseCategory.valueOf(requireNotNull(getString("category"))),
            title = requireNotNull(getString("title")),
            amount = requireNotNull(getLong("amount")),
            date = requireNotNull(getTimestamp("date")).toDate().time,
            notes = requireNotNull(getString("notes")),
            createdAt = requireNotNull(getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)).toDate().time,
            isSyncPending = metadata.hasPendingWrites(),
        ).also(::validate)
    } catch (error: Exception) {
        throw mapError(error)
    }

    private fun validate(expense: Expense) {
        if (expense.id.isBlank() || expense.id.length > 128 || '/' in expense.id ||
            expense.bikeId.isBlank() || expense.bikeId.length > 128 || '/' in expense.bikeId ||
            expense.bikeId == "." || expense.bikeId == ".." ||
            expense.title.trim().length !in 1..100 || expense.amount !in 1..1_000_000_000 ||
            expense.date !in 0..4102444800000L || expense.notes.trim().length > 1000
        ) throw ExpenseException(ExpenseFailure.InvalidData)
    }

    private fun mapError(error: Exception): ExpenseException {
        if (error is ExpenseException) return error
        return ExpenseException(when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> ExpenseFailure.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> ExpenseFailure.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> ExpenseFailure.AuthenticationExpired
            else -> ExpenseFailure.Unknown
        })
    }
}
