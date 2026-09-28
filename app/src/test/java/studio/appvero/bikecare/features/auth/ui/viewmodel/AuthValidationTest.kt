package studio.appvero.bikecare.features.auth.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.ui.screen.AuthFormState

class AuthValidationTest {
    @Test fun registrationRejectsMalformedEmailShortPasswordAndMismatch() {
        val result = AuthValidation.validate(
            AuthFormState(email = "rider@", password = "short", confirmPassword = "different"), true,
        )
        assertEquals(R.string.auth_invalid_email, result.emailError)
        assertEquals(R.string.auth_password_short, result.passwordError)
        assertEquals(R.string.auth_password_mismatch, result.confirmPasswordError)
        assertFalse(AuthValidation.isValid(result))
    }

    @Test fun loginDoesNotApplyNewAccountPasswordPolicy() {
        val result = AuthValidation.validate(AuthFormState(email = "rider@example.com", password = "old"), false)
        assertTrue(AuthValidation.isValid(result))
    }

    @Test fun validRegistrationAcceptsTrimmedEmailButPreservesPasswordWhitespace() {
        val password = " secret "
        val result = AuthValidation.validate(
            AuthFormState(email = " rider@example.com ", password = password, confirmPassword = password), true,
        )
        assertTrue(AuthValidation.isValid(result))
        assertEquals(password, result.password)
    }

    @Test fun emptyLoginRequiresBothFields() {
        val result = AuthValidation.validate(AuthFormState(), false)
        assertEquals(R.string.auth_invalid_email, result.emailError)
        assertEquals(R.string.auth_password_required, result.passwordError)
    }

    @Test fun embeddedEmailWhitespaceIsRejected() {
        assertNotNull(AuthValidation.emailError("ride r@example.com"))
    }

    @Test fun validationClearsStaleErrorsAfterCorrection() {
        val result = AuthValidation.validate(
            AuthFormState(email = "rider@example.com", password = "abcdef", confirmPassword = "abcdef",
                emailError = R.string.auth_invalid_email, passwordError = R.string.auth_password_short,
                confirmPasswordError = R.string.auth_password_mismatch, error = R.string.auth_network_error), true,
        )
        assertTrue(AuthValidation.isValid(result))
        assertNull(result.error)
    }
}
