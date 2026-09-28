package studio.appvero.bikecare.features.auth.ui.screen

data class LoginUiState(val form: AuthFormState = AuthFormState())

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object Submit : LoginEvent
    data object Register : LoginEvent
    data object ResetPassword : LoginEvent
    data object EffectHandled : LoginEvent
}

sealed interface LoginSideEffect {
    data object NavigateToResetPassword : LoginSideEffect
    data object NavigateToHome : LoginSideEffect
    data object NavigateToRegister : LoginSideEffect
}
