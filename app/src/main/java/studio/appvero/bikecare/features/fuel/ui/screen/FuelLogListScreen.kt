package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.fuel.domain.model.FuelLog
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import java.text.NumberFormat
import java.util.Locale
import studio.appvero.bikecare.core.localization.LocalLocale

@Composable
fun FuelLogListScreen(state: FuelLogListUiState, onEvent: (FuelLogListEvent) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        item {
            Text(localizedString(R.string.fuel_title), style = MaterialTheme.typography.headlineLarge)
            Text(localizedString(R.string.fuel_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { BikeCareButton(localizedString(R.string.fuel_add), { onEvent(FuelLogListEvent.AddLog) }) }
        when {
            state.isLoading -> item {
                Column(Modifier.fillMaxWidth().padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(localizedString(R.string.fuel_loading))
                }
            }
            state.error != null -> item {
                FuelMessageCard {
                    Text(localizedString(state.error), color = MaterialTheme.colorScheme.error)
                    BikeCareButton(localizedString(R.string.fuel_retry), { onEvent(FuelLogListEvent.Retry) }, outlined = true)
                }
            }
            state.logs.isEmpty() -> item {
                FuelMessageCard {
                    Text(localizedString(R.string.fuel_empty), style = MaterialTheme.typography.titleLarge)
                    Text(localizedString(R.string.fuel_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                items(state.logs, key = { it.id }) { log -> FuelLogCard(log, state.bikeNames[log.bikeId]) }
                if (state.logs.size == 100) item {
                    Text(localizedString(R.string.fuel_recent_limit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { BikeCareButton(localizedString(R.string.fuel_back), { onEvent(FuelLogListEvent.Back) }, outlined = true) }
    }
}

@Composable
private fun FuelMessageCard(content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            content()
        }
    }
}

@Composable
private fun FuelLogCard(log: FuelLog, bikeName: String?) {
    val number = NumberFormat.getNumberInstance(LocalLocale.current).apply { maximumFractionDigits = 2 }
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(localizedString(R.string.fuel_log_bike, bikeName ?: log.bikeId), style = MaterialTheme.typography.titleLarge)
            Text(localizedString(R.string.fuel_log_date, fuelDate(log.date)))
            Text(localizedString(R.string.fuel_log_type, log.fuelType))
            Text(localizedString(R.string.fuel_log_odometer, log.odometer))
            Text(localizedString(R.string.fuel_log_quantity, number.format(log.quantity)))
            Text(localizedString(R.string.fuel_log_price, number.format(log.pricePerLiter)))
            Text(localizedString(R.string.fuel_log_total, number.format(log.totalCost)))
            if (log.station.isNotBlank()) Text(localizedString(R.string.fuel_log_station, log.station))
            if (log.fullTank) Text(localizedString(R.string.fuel_log_full_tank))
            if (log.notes.isNotBlank()) Text(log.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (log.isSyncPending) Text(localizedString(R.string.fuel_sync_pending),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
