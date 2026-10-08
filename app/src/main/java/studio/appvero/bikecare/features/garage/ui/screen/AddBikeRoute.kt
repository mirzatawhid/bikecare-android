package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.garage.ui.viewmodel.AddBikeViewModel

@Composable
fun AddBikeRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddBikeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(AddBikeEvent.EffectHandled)
            when (effect) {
                AddBikeSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    AddBikeScreen(state = state, onEvent = viewModel::onEvent)
}
