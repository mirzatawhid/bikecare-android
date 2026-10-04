package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.maintenance.ui.viewmodel.MaintenanceViewModel

@Composable
fun MaintenanceRoute(
    onBack: () -> Unit,
    onOpenGarage: () -> Unit,
    onLogService: (bikeId: String, maintenanceId: String?) -> Unit,
    onReminder: (bikeId: String) -> Unit,
    onOpenItem: (bikeId: String, maintenanceId: String) -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onEvent(MaintenanceEvent.RefreshDate)
        }
    }
    LaunchedEffect(viewModel, lifecycle, onBack, onOpenGarage, onLogService, onReminder, onOpenItem) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(MaintenanceEvent.EffectHandled)
            when (effect) {
                MaintenanceSideEffect.Back -> onBack()
                MaintenanceSideEffect.OpenGarage -> onOpenGarage()
                is MaintenanceSideEffect.LogService -> onLogService(effect.bikeId, effect.maintenanceId)
                is MaintenanceSideEffect.Reminder -> onReminder(effect.bikeId)
                is MaintenanceSideEffect.OpenItem -> onOpenItem(effect.bikeId, effect.maintenanceId)
            }
        }
    }
    MaintenanceScreen(state, viewModel::onEvent)
}
