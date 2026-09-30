package studio.appvero.bikecare.features.auth.ui.viewmodel

import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.data.repository.AuthException
import studio.appvero.bikecare.features.auth.data.repository.AuthFailure

internal fun authError(error: Exception): Int = when ((error as? AuthException)?.failure) {
    AuthFailure.InvalidEmail -> R.string.auth_invalid_email
    AuthFailure.InvalidCredentials -> R.string.auth_invalid_credentials
    AuthFailure.AccountExists -> R.string.auth_account_exists
    AuthFailure.WeakPassword -> R.string.auth_password_policy
    AuthFailure.AccountDisabled -> R.string.auth_account_disabled
    AuthFailure.ProviderDisabled -> R.string.auth_provider_disabled
    AuthFailure.Network -> R.string.auth_network_error
    AuthFailure.TooManyRequests -> R.string.auth_too_many_requests
    else -> R.string.auth_generic_error
}