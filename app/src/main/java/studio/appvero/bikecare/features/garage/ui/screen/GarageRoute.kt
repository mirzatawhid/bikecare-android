package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.garage.ui.viewmodel.GarageViewModel

@Composable
fun GarageRoute(viewModel: GarageViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val addBikeForm = viewModel.addBikeForm.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bikeAddedMessage = localizedString(R.string.garage_bike_saved)

    LaunchedEffect(viewModel, bikeAddedMessage) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                GarageSideEffect.BikeAdded -> snackbarHostState.showSnackbar(bikeAddedMessage)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        GarageScreen(state.value, addBikeForm.value, viewModel::onEvent)
        SnackbarHost(hostState = snackbarHostState)
    }
}
