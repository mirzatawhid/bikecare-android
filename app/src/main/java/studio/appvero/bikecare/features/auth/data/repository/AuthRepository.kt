package studio.appvero.bikecare.features.auth.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.auth.data.model.AuthUser

interface AuthRepository {
    suspend fun register(email: String, password: String): AuthUser
    suspend fun login(email: String, password: String): AuthUser
    suspend fun logout()
    suspend fun resetPassword(email: String)
    fun observeAuthState(): Flow<AuthUser?>
    fun getCurrentUser(): AuthUser?
    suspend fun sendVerificationEmail()
    suspend fun refreshUser(): AuthUser?
}

enum class AuthFailure {
    InvalidEmail, InvalidCredentials, AccountExists, WeakPassword, AccountDisabled,
    ProviderDisabled, Network, TooManyRequests, Unknown,
}

class AuthException(val failure: AuthFailure) : Exception(failure.name)