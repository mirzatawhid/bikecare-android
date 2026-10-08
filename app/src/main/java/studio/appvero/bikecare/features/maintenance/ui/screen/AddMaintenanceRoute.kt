package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.maintenance.ui.viewmodel.AddMaintenanceViewModel

@Composable
fun AddMaintenanceRoute(onNavigateBack: () -> Unit, viewModel: AddMaintenanceViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(AddMaintenanceEvent.EffectHandled)
            when (effect) {
                AddMaintenanceSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    AddMaintenanceScreen(state, viewModel::onEvent)
}
