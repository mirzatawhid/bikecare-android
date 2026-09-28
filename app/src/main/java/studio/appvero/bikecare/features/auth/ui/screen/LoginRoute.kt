package studio.appvero.bikecare.features.auth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.auth.ui.viewmodel.LoginViewModel

@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    BackHandler(enabled = state.form.busy) { /* Wait for the active authentication request. */ }
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            // Keep a pending navigation effect while the app is backgrounded.
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(LoginEvent.EffectHandled)
            when (effect) {
                LoginSideEffect.NavigateToResetPassword -> onNavigateToResetPassword()
                LoginSideEffect.NavigateToHome -> onNavigateToHome()
                LoginSideEffect.NavigateToRegister -> onNavigateToRegister()
            }
        }
    }
    LoginScreen(state, viewModel::onEvent)
}
