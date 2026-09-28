package studio.appvero.bikecare.features.auth.ui.viewmodel

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import studio.appvero.bikecare.R

internal fun authError(error: Exception): Int = when (error) {
    is FirebaseNetworkException -> R.string.auth_network_error
    is FirebaseTooManyRequestsException -> R.string.auth_too_many_requests
    is FirebaseAuthException -> when (error.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> R.string.auth_account_exists
        "ERROR_WEAK_PASSWORD" -> R.string.auth_password_policy
        "ERROR_INVALID_EMAIL" -> R.string.auth_invalid_email
        "ERROR_USER_DISABLED" -> R.string.auth_account_disabled
        "ERROR_OPERATION_NOT_ALLOWED" -> R.string.auth_provider_disabled
        "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL",
        "ERROR_INVALID_LOGIN_CREDENTIALS" -> R.string.auth_invalid_credentials
        else -> R.string.auth_generic_error
    }
    else -> R.string.auth_generic_error
}
