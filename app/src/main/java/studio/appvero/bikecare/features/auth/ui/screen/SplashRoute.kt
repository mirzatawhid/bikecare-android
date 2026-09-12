package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.appvero.bikecare.features.auth.ui.viewmodel.SplashViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SplashRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                SplashSideEffect.NavigateToHome -> {
                    onNavigateToHome()
                }

                SplashSideEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    SplashScreen(
        uiState = uiState.value,
        onEvent = viewModel::onEvent
    )
}