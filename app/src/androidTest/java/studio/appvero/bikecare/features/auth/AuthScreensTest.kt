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

    @Test fun loginOffersRegistrationAndGoogleSignIn() {
        val events = mutableListOf<LoginEvent>()
        compose.setContent { BikeCareTheme { LoginScreen(LoginUiState(), events::add) } }
        compose.onNodeWithText("Sign in with Google").performScrollTo().performClick()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        assertTrue(events.contains(LoginEvent.GoogleSignIn))
        assertTrue(events.contains(LoginEvent.Register))
    }

    @Test fun inFlightGoogleRequestDisablesOtherActions() {
        compose.setContent {
            BikeCareTheme { LoginScreen(LoginUiState(AuthFormState(isGoogleLoading = true)), {}) }
        }
        compose.onNodeWithText("Sign in").assertIsNotEnabled()
        compose.onNodeWithText("Sign in with Google").assertIsNotEnabled()
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
}
