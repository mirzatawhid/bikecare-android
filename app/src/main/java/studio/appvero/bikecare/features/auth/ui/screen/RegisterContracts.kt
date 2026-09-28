package studio.appvero.bikecare.features.auth.ui.screen

data class RegisterUiState(val form: AuthFormState = AuthFormState())

sealed interface RegisterEvent {
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterEvent
    data object TogglePasswordVisibility : RegisterEvent
    data object Submit : RegisterEvent
    data object GoogleSignIn : RegisterEvent
    data class GoogleTokenReceived(val token: String) : RegisterEvent
    data class GoogleFailed(val message: Int?) : RegisterEvent
    data object Login : RegisterEvent
    data object EffectHandled : RegisterEvent
}

sealed interface RegisterSideEffect {
    data object NavigateToHome : RegisterSideEffect
    data object NavigateToLogin : RegisterSideEffect
    data object LaunchGoogleSignIn : RegisterSideEffect
}
