package studio.appvero.bikecare.features.auth.ui.screen

sealed interface SplashUiState {

    data object Loading : SplashUiState

    data class Error(
        val message: String
    ) : SplashUiState
}

sealed interface SplashEvent {

    data object CheckAuthentication : SplashEvent

    data object Retry : SplashEvent
}

sealed interface SplashSideEffect {

    data object NavigateToHome : SplashSideEffect

    data object NavigateToLogin : SplashSideEffect
}