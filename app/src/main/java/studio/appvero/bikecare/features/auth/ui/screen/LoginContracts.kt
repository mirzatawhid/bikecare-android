package studio.appvero.bikecare.features.auth.ui.screen

data class LoginUiState(val form: AuthFormState = AuthFormState())

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object Submit : LoginEvent
    data object GoogleSignIn : LoginEvent
    data class GoogleTokenReceived(val token: String) : LoginEvent
    data class GoogleFailed(val message: Int?) : LoginEvent
    data object Register : LoginEvent
    data object ResetPassword : LoginEvent
    data object EffectHandled : LoginEvent
}

sealed interface LoginSideEffect {
    data object NavigateToHome : LoginSideEffect
    data object NavigateToRegister : LoginSideEffect
    data object LaunchGoogleSignIn : LoginSideEffect
}
