package studio.appvero.bikecare.features.auth.ui.screen

data class ForgotPasswordUiState(val form: AuthFormState = AuthFormState(), val sent: Boolean = false)

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
    data object Login : ForgotPasswordEvent
    data object EffectHandled : ForgotPasswordEvent
}

sealed interface ForgotPasswordSideEffect {
    data object NavigateToLogin : ForgotPasswordSideEffect
}