package studio.appvero.bikecare.features.auth.ui.viewmodel

import io.github.jan.supabase.auth.exception.AuthRestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import studio.appvero.bikecare.R
import java.io.IOException

internal fun authError(error: Exception): Int = when (error) {
    is AuthRestException -> authErrorCode(error.errorCode?.value)
    is IOException, is HttpRequestTimeoutException -> R.string.auth_network_error
    else -> R.string.auth_generic_error
}

internal fun authErrorCode(code: String?): Int = when (code) {
    "email_exists", "user_already_exists" -> R.string.auth_account_exists
    "weak_password", "same_password" -> R.string.auth_password_policy
    "email_address_invalid" -> R.string.auth_invalid_email
    "user_banned" -> R.string.auth_account_disabled
    "signup_disabled", "email_provider_disabled" -> R.string.auth_provider_disabled
    "invalid_credentials", "user_not_found" -> R.string.auth_invalid_credentials
    "email_not_confirmed" -> R.string.auth_confirmation_required
    "over_request_rate_limit", "over_email_send_rate_limit" -> R.string.auth_too_many_requests
    "otp_expired", "otp_disabled" -> R.string.auth_invalid_reset_code
    "request_timeout" -> R.string.auth_network_error
    else -> R.string.auth_generic_error
}
