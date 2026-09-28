package studio.appvero.bikecare.features.auth.ui.screen

import androidx.annotation.StringRes

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val error: Int? = null,
    @param:StringRes val message: Int? = null,
) {
    val busy: Boolean get() = isLoading
}
