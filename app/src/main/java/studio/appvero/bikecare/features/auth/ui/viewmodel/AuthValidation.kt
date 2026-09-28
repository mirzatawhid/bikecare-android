package studio.appvero.bikecare.features.auth.ui.viewmodel

import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.ui.screen.AuthFormState

internal object AuthValidation {
    fun emailError(email: String): Int? =
        if (Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) null
        else R.string.auth_invalid_email

    fun validate(state: AuthFormState, registering: Boolean): AuthFormState = state.copy(
        emailError = emailError(state.email),
        passwordError = when {
            state.password.isEmpty() -> R.string.auth_password_required
            registering && state.password.length < 6 -> R.string.auth_password_short
            else -> null
        },
        confirmPasswordError = if (registering && state.confirmPassword != state.password)
            R.string.auth_password_mismatch else null,
        error = null,
        message = null,
    )

    fun isValid(state: AuthFormState): Boolean =
        state.emailError == null && state.passwordError == null && state.confirmPasswordError == null
}
