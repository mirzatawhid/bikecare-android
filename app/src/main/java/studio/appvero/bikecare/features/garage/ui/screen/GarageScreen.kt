package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.garage.ui.components.previewBike
import studio.appvero.bikecare.ui.theme.*

@Composable
fun GarageScreen(state: GarageUiState, onEvent: (GarageEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        item {
            Text(localizedString(R.string.nav_garage), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(localizedString(R.string.garage_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            state.isLoading -> item {
                Column(Modifier.fillMaxWidth().padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    CircularProgressIndicator()
                    Text(localizedString(R.string.garage_loading))
                }
            }
            state.error != null -> item {
                GarageMessage(localizedString(state.error), isError = true) {
                    OutlinedButton(onClick = { onEvent(GarageEvent.Retry) }) { Text(localizedString(R.string.garage_retry)) }
                }
            }
            state.bikes.isEmpty() -> item {
                GarageMessage(localizedString(R.string.garage_empty)) {
                    Text(localizedString(R.string.garage_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AddBikeButton { onEvent(GarageEvent.AddBike) }
                }
            }
            else -> {
                item { AddBikeButton { onEvent(GarageEvent.AddBike) } }
                items(state.bikes, key = { it.id }) { BikeCard(it) }
            }
        }
    }
}
@Preview(
    name = "Garage - Bikes",
    showBackground = true,
)
@Composable
private fun GarageScreenWithBikesPreview() {
    BikeCareTheme {
        GarageScreen(
            state = GarageUiState(
                isLoading = false,
                error = null,
                bikes = listOf(
                    previewBike(),
                    previewBike(
                        id = "bike-2",
                        brand = "Honda",
                        model = "CB Hornet 160R",
                        year = 2022,
                        registrationNumber = "DHAKA METRO-HA-34-5678",
                        initialOdometer = 5_000L,
                        currentOdometer = 21_450L,
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(
    name = "Garage - Empty",
    showBackground = true,
)
@Composable
private fun GarageScreenEmptyPreview() {
    BikeCareTheme {
        GarageScreen(
            state = GarageUiState(
                isLoading = false,
                bikes = emptyList(),
                error = null,
            ),
            onEvent = {},
        )
    }
}

@Preview(
    name = "Garage - Loading",
    showBackground = true,
)
@Composable
private fun GarageScreenLoadingPreview() {
    BikeCareTheme {
        GarageScreen(
            state = GarageUiState(
                isLoading = true,
                bikes = emptyList(),
                error = null,
            ),
            onEvent = {},
        )
    }
}

@Composable
private fun GarageMessage(message: String, isError: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(message, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun AddBikeButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget),
        shape = BikeCarePillShape,
        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.action, contentColor = AppTheme.colors.onAction)) {
        Text(localizedString(R.string.garage_add_bike))
    }
}

@Composable
private fun BikeCard(bike: Bike) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("${bike.brand} ${bike.model}", style = MaterialTheme.typography.titleLarge)
            Text(localizedString(R.string.garage_year, bike.year), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(bike.registrationNumber.ifBlank { localizedString(R.string.garage_unregistered) })
            HorizontalDivider()
            Text(localizedString(R.string.garage_odometer, bike.currentOdometer), style = MaterialTheme.typography.titleMedium)
            if (bike.isSyncPending) {
                Text(localizedString(R.string.garage_sync_pending), color = AppTheme.colors.warning,
                    style = MaterialTheme.typography.bodyMedium)
            }
            Text(localizedString(if (bike.isActive) R.string.garage_active else R.string.garage_inactive),
                color = if (bike.isActive) AppTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
