package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.fuel.ui.viewmodel.FuelLogListViewModel

@Composable
fun FuelLogListRoute(
    onNavigateToAddLog: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FuelLogListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(FuelLogListEvent.EffectHandled)
            when (effect) {
                FuelLogListSideEffect.NavigateToAddLog -> onNavigateToAddLog()
                FuelLogListSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    FuelLogListScreen(state, viewModel::onEvent)
}
