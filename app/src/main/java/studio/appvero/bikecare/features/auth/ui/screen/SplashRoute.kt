package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.auth.ui.viewmodel.SplashViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SplashRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToVerifyEmail: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(SplashEvent.EffectHandled)
            when (effect) {
                SplashSideEffect.NavigateToVerifyEmail -> onNavigateToVerifyEmail()
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
