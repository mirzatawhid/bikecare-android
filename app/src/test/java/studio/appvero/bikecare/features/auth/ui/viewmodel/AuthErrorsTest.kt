package studio.appvero.bikecare.features.auth.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.data.repository.AuthException
import studio.appvero.bikecare.features.auth.data.repository.AuthFailure

class AuthErrorsTest {
    @Test fun invalidCredentialsDoNotRevealAccountExistence() {
        assertEquals(R.string.auth_invalid_credentials, authError(AuthException(AuthFailure.InvalidCredentials)))
    }

    @Test fun duplicateAndWeakPasswordHaveActionableErrors() {
        assertEquals(R.string.auth_account_exists, authError(AuthException(AuthFailure.AccountExists)))
        assertEquals(R.string.auth_password_policy, authError(AuthException(AuthFailure.WeakPassword)))
    }

    @Test fun disabledAccountAndProviderHaveActionableErrors() {
        assertEquals(R.string.auth_account_disabled, authError(AuthException(AuthFailure.AccountDisabled)))
        assertEquals(R.string.auth_provider_disabled, authError(AuthException(AuthFailure.ProviderDisabled)))
    }

    @Test fun rateLimitsAndNetworkFailuresAreDistinct() {
        assertEquals(R.string.auth_too_many_requests, authError(AuthException(AuthFailure.TooManyRequests)))
        assertEquals(R.string.auth_network_error, authError(AuthException(AuthFailure.Network)))
    }

    @Test fun unknownErrorsDoNotExposeServerDetails() {
        assertEquals(R.string.auth_generic_error, authError(AuthException(AuthFailure.Unknown)))
        assertEquals(R.string.auth_generic_error, authError(IllegalStateException("private detail")))
    }
}