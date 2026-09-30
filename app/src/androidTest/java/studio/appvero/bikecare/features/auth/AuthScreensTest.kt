package studio.appvero.bikecare.features.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import studio.appvero.bikecare.features.auth.ui.screen.*
import studio.appvero.bikecare.ui.theme.BikeCareTheme

class AuthScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loginOffersRegistrationAndPasswordRecovery() {
        val events = mutableListOf<LoginEvent>()
        compose.setContent { BikeCareTheme { LoginScreen(LoginUiState(), events::add) } }
        compose.onNodeWithText("Sign in with Google").assertDoesNotExist()
        compose.onNodeWithText("Forgot password?").performScrollTo().performClick()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        assertTrue(events.contains(LoginEvent.ResetPassword))
        assertTrue(events.contains(LoginEvent.Register))
    }

    @Test fun inFlightRequestDisablesOtherActions() {
        compose.setContent {
            BikeCareTheme { LoginScreen(LoginUiState(AuthFormState(isLoading = true)), {}) }
        }
        compose.onNodeWithText("Please wait…").assertIsNotEnabled()
        compose.onNodeWithText("Forgot password?").assertIsNotEnabled()
        compose.onNodeWithText("Create account").assertIsNotEnabled()
    }

    @Test fun registerMasksPasswordsAndEmitsVisibilityEvent() {
        val events = mutableListOf<RegisterEvent>()
        compose.setContent {
            BikeCareTheme {
                RegisterScreen(RegisterUiState(AuthFormState(password = "secret1", confirmPassword = "secret1")), events::add)
            }
        }
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)).assertCountEquals(2)
        compose.onAllNodesWithText("Show")[0].performScrollTo().performClick()
        assertTrue(events.contains(RegisterEvent.TogglePasswordVisibility))
    }

    @Test fun recoveryRequestsHostedEmailLink() {
        val events = mutableListOf<ForgotPasswordEvent>()
        compose.setContent {
            BikeCareTheme { ForgotPasswordScreen(ForgotPasswordUiState(), events::add) }
        }
        compose.onNodeWithText("Email address").performScrollTo().performTextInput("rider@example.com")
        assertTrue(events.contains(ForgotPasswordEvent.EmailChanged("rider@example.com")))
        compose.onNodeWithText("Reset code").assertDoesNotExist()
        compose.onNodeWithText("Send reset email").performScrollTo().performClick()
        assertTrue(events.contains(ForgotPasswordEvent.Submit))
    }
}
