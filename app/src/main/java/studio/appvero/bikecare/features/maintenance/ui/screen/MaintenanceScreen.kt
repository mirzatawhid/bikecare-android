package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceCategory
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceLog
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceServiceType
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing

@Composable
fun MaintenanceScreen(state: MaintenanceUiState, onEvent: (MaintenanceEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        item {
            Text(localizedString(R.string.maintenance_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(localizedString(R.string.maintenance_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            BikeCareButton(localizedString(R.string.maintenance_add), onClick = { onEvent(MaintenanceEvent.AddLog) })
        }
        when {
            state.isLoading -> item {
                Column(Modifier.fillMaxWidth().padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    CircularProgressIndicator()
                    Text(localizedString(R.string.maintenance_loading))
                }
            }
            state.error != null -> item {
                MessageCard(localizedString(state.error)) {
                    BikeCareButton(localizedString(R.string.maintenance_retry),
                        onClick = { onEvent(MaintenanceEvent.Retry) }, outlined = true)
                }
            }
            state.logs.isEmpty() -> item {
                MessageCard(localizedString(R.string.maintenance_empty)) {
                    Text(localizedString(R.string.maintenance_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                items(state.logs, key = { it.id }) { log -> LogCard(log, state.bikeNames[log.bikeId]) }
                if (state.logs.size == 100) item {
                    Text(localizedString(R.string.maintenance_recent_limit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MessageCard(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun LogCard(log: MaintenanceLog, bikeName: String?) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(log.title, style = MaterialTheme.typography.titleLarge)
            Text(localizedString(R.string.maintenance_log_bike, bikeName ?: log.bikeId),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${localizedString(log.category.label())} · ${localizedString(log.serviceType.label())}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(localizedString(R.string.maintenance_log_date, maintenanceDate(log.date)))
            Text(localizedString(R.string.maintenance_log_odometer, log.odometer))
            Text(localizedString(R.string.maintenance_log_cost, log.cost))
            if (log.provider.isNotBlank()) Text(localizedString(R.string.maintenance_log_provider, log.provider))
            if (log.description.isNotBlank()) Text(log.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (log.nextServiceOdometer != null || log.nextServiceDate != null) {
                HorizontalDivider()
                log.nextServiceOdometer?.let { Text(localizedString(R.string.maintenance_log_next_odometer, it)) }
                log.nextServiceDate?.let { Text(localizedString(R.string.maintenance_log_next_date, maintenanceDate(it))) }
            }
            if (log.isSyncPending) {
                Text(localizedString(R.string.maintenance_sync_pending), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

internal fun MaintenanceCategory.label(): Int = when (this) {
    MaintenanceCategory.ENGINE -> R.string.maintenance_category_engine
    MaintenanceCategory.BRAKES -> R.string.maintenance_category_brakes
    MaintenanceCategory.TIRES -> R.string.maintenance_category_tires
    MaintenanceCategory.ELECTRICAL -> R.string.maintenance_category_electrical
    MaintenanceCategory.GENERAL -> R.string.maintenance_category_general
}

internal fun MaintenanceServiceType.label(): Int = when (this) {
    MaintenanceServiceType.PREVENTIVE -> R.string.maintenance_type_preventive
    MaintenanceServiceType.REPAIR -> R.string.maintenance_type_repair
    MaintenanceServiceType.INSPECTION -> R.string.maintenance_type_inspection
}
