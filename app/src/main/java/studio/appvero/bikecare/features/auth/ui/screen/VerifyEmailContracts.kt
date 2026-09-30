package studio.appvero.bikecare.features.auth.ui.screen

data class VerifyEmailUiState(val form: AuthFormState = AuthFormState(), val email: String = "")

sealed interface VerifyEmailEvent {
    data object Refresh : VerifyEmailEvent
    data object Resend : VerifyEmailEvent
    data object Logout : VerifyEmailEvent
    data object EffectHandled : VerifyEmailEvent
}

sealed interface VerifyEmailSideEffect {
    data object NavigateToHome : VerifyEmailSideEffect
    data object NavigateToLogin : VerifyEmailSideEffect
}