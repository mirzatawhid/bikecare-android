package studio.appvero.bikecare.features.auth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.auth.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    BackHandler(enabled = state.form.busy) { /* Wait for the active authentication request. */ }
    LaunchedEffect(viewModel, context, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            // Keep a pending navigation effect while the app is backgrounded.
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(RegisterEvent.EffectHandled)
            when (effect) {
                RegisterSideEffect.NavigateToHome -> onNavigateToHome()
                RegisterSideEffect.NavigateToLogin -> onNavigateToLogin()
                RegisterSideEffect.LaunchGoogleSignIn -> requestGoogleSignIn(
                    context,
                    onToken = { viewModel.onEvent(RegisterEvent.GoogleTokenReceived(it)) },
                    onFailure = { viewModel.onEvent(RegisterEvent.GoogleFailed(it)) },
                )
            }
        }
    }
    RegisterScreen(state, viewModel::onEvent)
}
