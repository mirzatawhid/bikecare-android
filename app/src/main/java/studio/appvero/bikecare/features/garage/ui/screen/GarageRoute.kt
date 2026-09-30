package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.garage.ui.viewmodel.GarageViewModel

@Composable
fun GarageRoute(onNavigateToAddBike: () -> Unit, viewModel: GarageViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(GarageEvent.EffectHandled)
            when (effect) {
                GarageSideEffect.NavigateToAddBike -> onNavigateToAddBike()
            }
        }
    }
    GarageScreen(state, viewModel::onEvent)
}
