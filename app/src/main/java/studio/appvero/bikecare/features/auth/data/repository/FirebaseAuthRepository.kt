package studio.appvero.bikecare.features.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import studio.appvero.bikecare.features.auth.data.model.AuthUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {
    // Reloading verification does not necessarily trigger an auth state callback.
    private val refreshRevision = MutableStateFlow(0L)

    override fun observeAuthState() = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.combine(refreshRevision) { _, _ -> getCurrentUser() }.distinctUntilChanged()

    override fun getCurrentUser() = auth.currentUser?.toDomain()

    override suspend fun register(email: String, password: String): AuthUser = mapped {
        requireNotNull(auth.createUserWithEmailAndPassword(email.trim(), password).await().user).toDomain()
    }

    override suspend fun login(email: String, password: String): AuthUser = mapped {
        requireNotNull(auth.signInWithEmailAndPassword(email.trim(), password).await().user).toDomain()
    }

    override suspend fun logout() = mapped { auth.signOut() }

    override suspend fun resetPassword(email: String) = mapped {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
        } catch (error: FirebaseAuthException) {
            // Neutral confirmation also protects projects without enumeration protection.
            if (error.errorCode != "ERROR_USER_NOT_FOUND") throw error
        }
        Unit
    }

    override suspend fun sendVerificationEmail() = mapped {
        val user = auth.currentUser ?: throw AuthException(AuthFailure.InvalidCredentials)
        user.sendEmailVerification().await()
        Unit
    }

    override suspend fun refreshUser(): AuthUser? = mapped {
        val user = auth.currentUser ?: return@mapped null
        try {
            user.reload().await()
            val refreshed = auth.currentUser
            if (refreshed?.uid != user.uid) return@mapped getCurrentUser()
            if (refreshed.isEmailVerified) refreshed.getIdToken(true).await()
            refreshRevision.value++
            getCurrentUser()
        } catch (error: FirebaseAuthException) {
            if (error.errorCode in setOf("ERROR_USER_DISABLED", "ERROR_USER_NOT_FOUND", "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN")) {
                if (auth.currentUser?.uid == user.uid) auth.signOut()
            }
            throw error
        }
    }

    private fun FirebaseUser.toDomain() = AuthUser(uid, email, isEmailVerified)

    private suspend fun <T> mapped(block: suspend () -> T): T = try {
        block()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: AuthException) {
        throw error
    } catch (error: Exception) {
        throw AuthException(when (error) {
            is FirebaseNetworkException -> AuthFailure.Network
            is FirebaseTooManyRequestsException -> AuthFailure.TooManyRequests
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthFailure.InvalidEmail
                "ERROR_EMAIL_ALREADY_IN_USE" -> AuthFailure.AccountExists
                "ERROR_WEAK_PASSWORD" -> AuthFailure.WeakPassword
                "ERROR_USER_DISABLED" -> AuthFailure.AccountDisabled
                "ERROR_OPERATION_NOT_ALLOWED" -> AuthFailure.ProviderDisabled
                "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN" -> AuthFailure.InvalidCredentials
                "ERROR_TOO_MANY_REQUESTS" -> AuthFailure.TooManyRequests
                else -> AuthFailure.Unknown
            }
            else -> AuthFailure.Unknown
        })
    }
}
