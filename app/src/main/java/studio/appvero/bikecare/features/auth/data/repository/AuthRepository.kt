package studio.appvero.bikecare.features.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password).awaitResult()
    }

    suspend fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).awaitResult()
    }

    suspend fun signInWithGoogle(idToken: String) {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).awaitResult()
    }

    suspend fun sendPasswordReset(email: String) {
        firebaseAuth.sendPasswordResetEmail(email.trim()).awaitResult()
    }
}

// Firebase Tasks cannot be cancelled; cancellation stops delivery to the caller.
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Authentication failed"))
        }
    }
}
