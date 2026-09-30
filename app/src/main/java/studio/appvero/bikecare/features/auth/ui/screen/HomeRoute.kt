package studio.appvero.bikecare.features.auth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.auth.ui.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerifyEmail: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    BackHandler(enabled = state.form.busy) {}
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onEvent(HomeEvent.Refresh)
        }
    }
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(HomeEvent.EffectHandled)
            when (effect) {
                HomeSideEffect.NavigateToLogin -> onNavigateToLogin()
                HomeSideEffect.NavigateToVerifyEmail -> onNavigateToVerifyEmail()
            }
        }
    }
    AuthSuccessScreen(state, viewModel::onEvent)
}