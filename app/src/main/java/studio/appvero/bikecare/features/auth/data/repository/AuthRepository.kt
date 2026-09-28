package studio.appvero.bikecare.features.auth.data.repository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject

enum class RegistrationResult { SignedIn, ConfirmationRequired }

class AuthRepository @Inject constructor(private val auth: Auth) {
    suspend fun isUserLoggedIn(): Boolean {
        // Restoration is asynchronous; never use a preference as an authentication flag.
        auth.awaitInitialization()
        return auth.currentSessionOrNull() != null
    }

    suspend fun login(email: String, password: String) {
        auth.awaitInitialization()
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun register(email: String, password: String): RegistrationResult {
        auth.awaitInitialization()
        val confirmation = auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        return if (confirmation == null && auth.currentSessionOrNull() != null) {
            RegistrationResult.SignedIn
        } else RegistrationResult.ConfirmationRequired
    }
}
