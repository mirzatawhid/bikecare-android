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

    @Test fun recoveryRequiresCodeAndMasksBothPasswords() {
        val events = mutableListOf<ResetPasswordEvent>()
        compose.setContent {
            BikeCareTheme {
                ResetPasswordScreen(ResetPasswordUiState(step = PasswordResetStep.CodeAndPassword), events::add)
            }
        }
        compose.onNodeWithText("Reset code").performScrollTo().performTextInput("123456")
        assertTrue(events.contains(ResetPasswordEvent.CodeChanged("123456")))
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)).assertCountEquals(2)
        compose.onNodeWithText("Update password").performScrollTo().performClick()
        assertTrue(events.contains(ResetPasswordEvent.Submit))
    }
}
