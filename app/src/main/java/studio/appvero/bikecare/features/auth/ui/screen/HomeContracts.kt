package studio.appvero.bikecare.features.auth.ui.screen

data class HomeUiState(val form: AuthFormState = AuthFormState())
sealed interface HomeEvent {
    data object Logout : HomeEvent
    data object Refresh : HomeEvent
    data object EffectHandled : HomeEvent
}
sealed interface HomeSideEffect {
    data object NavigateToLogin : HomeSideEffect
    data object NavigateToVerifyEmail : HomeSideEffect
}