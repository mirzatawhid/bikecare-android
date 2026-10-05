package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.*
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.maintenance.domain.model.*
import studio.appvero.bikecare.features.maintenance.ui.viewmodel.DUE_SOON_DAYS
import studio.appvero.bikecare.features.maintenance.ui.viewmodel.DUE_SOON_KM
import studio.appvero.bikecare.features.maintenance.ui.viewmodel.assess
import studio.appvero.bikecare.ui.components.BikeCareIconButton
import studio.appvero.bikecare.ui.components.BikeCarePrimaryButton
import studio.appvero.bikecare.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

@Composable
fun MaintenanceScreen(state: MaintenanceUiState, onEvent: (MaintenanceEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BikeCareIconButton(localizedString(R.string.maintenance_back), { onEvent(MaintenanceEvent.Back) }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null)
                    }
                    Text(localizedString(R.string.maintenance_title), style = MaterialTheme.typography.headlineLarge)
                }
                state.bike?.let {
                    Text(it.nickname ?: "${it.brand} ${it.model}", style = MaterialTheme.typography.headlineSmall)
                }
                Text(localizedString(R.string.maintenance_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.bike != null) item(key = "actions") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                MaintenanceAction(R.string.maintenance_log_service, R.drawable.ic_care, Modifier.weight(1f)) {
                    onEvent(MaintenanceEvent.LogService())
                }
                MaintenanceAction(R.string.maintenance_reminder, R.drawable.ic_reminder, Modifier.weight(1f)) {
                    onEvent(MaintenanceEvent.Reminder)
                }
            }
        }
        when (state) {
            is MaintenanceUiState.Loading -> {
                item(key = "loading") {
                    Column(Modifier.fillMaxWidth().padding(AppSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        CircularProgressIndicator(color = AppTheme.colors.action)
                        Text(localizedString(R.string.maintenance_loading))
                    }
                }
                state.previousContent?.let { maintenanceContent(it, onEvent) }
            }
            is MaintenanceUiState.Error -> {
                item(key = "error") {
                    MaintenanceMessage(localizedString(state.message), isError = true) {
                        BikeCarePrimaryButton(localizedString(R.string.garage_retry), { onEvent(MaintenanceEvent.Retry) })
                    }
                }
                state.previousContent?.let { maintenanceContent(it, onEvent) }
            }
            MaintenanceUiState.NoBike -> item(key = "no-bike") {
                MaintenanceMessage(localizedString(R.string.maintenance_no_bike)) {
                    Text(localizedString(R.string.maintenance_no_bike_body))
                    BikeCarePrimaryButton(localizedString(R.string.nav_garage), { onEvent(MaintenanceEvent.OpenGarage) })
                }
            }
            is MaintenanceUiState.Empty -> {
                item(key = "summary") { MaintenanceSummary(0, 0, 0) }
                item(key = "empty") {
                    MaintenanceMessage(localizedString(R.string.maintenance_empty)) {
                        Text(localizedString(R.string.maintenance_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is MaintenanceUiState.Content -> maintenanceContent(state, onEvent)
        }
    }
}

private fun LazyListScope.maintenanceContent(state: MaintenanceUiState.Content, onEvent: (MaintenanceEvent) -> Unit) {
    item(key = "summary") { MaintenanceSummary(state.overdueCount, state.dueSoonCount, state.upToDateCount) }
    item(key = "priority") {
        PriorityCard(state.priorityItem, state.bike,
            onLogService = { onEvent(MaintenanceEvent.LogService(state.priorityItem.item.id)) })
    }
    item(key = "list-title") {
        Text(localizedString(R.string.maintenance_items), style = MaterialTheme.typography.headlineSmall)
    }
    items(state.items, key = { "maintenance:${it.item.id}" }) { item ->
        MaintenanceRow(item, onClick = { onEvent(MaintenanceEvent.OpenItem(item.item.id)) })
    }
}

@Composable
private fun MaintenanceSummary(overdue: Int, dueSoon: Int, upToDate: Int) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        SummaryCard(MaintenanceStatus.OVERDUE, overdue, Modifier.weight(1f))
        SummaryCard(MaintenanceStatus.DUE_SOON, dueSoon, Modifier.weight(1f))
        SummaryCard(MaintenanceStatus.UP_TO_DATE, upToDate, Modifier.weight(1f))
    }
}

@Composable
private fun MaintenanceAction(label: Int, icon: Int, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large,
        modifier = modifier.widthIn(min = AppSpacing.xxl * 3).heightIn(min = AppDimensions.minimumTouchTarget)) {
        Row(Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs, Alignment.CenterHorizontally)) {
            Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary)
            Text(localizedString(label), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SummaryCard(status: MaintenanceStatus, count: Int, modifier: Modifier) {
    Surface(shape = MaterialTheme.shapes.large, modifier = modifier.widthIn(min = AppDimensions.minimumTouchTarget * 2)) {
        Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(when (status) {
                    MaintenanceStatus.OVERDUE -> "!"
                    MaintenanceStatus.DUE_SOON -> "◷"
                    MaintenanceStatus.UP_TO_DATE -> "✓"
                }, color = statusColor(status), style = MaterialTheme.typography.titleLarge)
                Text(formatNumber(count.toLong()), style = MaterialTheme.typography.headlineSmall)
            }
            Text(localizedString(statusLabel(status)), style = MaterialTheme.typography.labelLarge, color = statusColor(status))
        }
    }
}

@Composable
private fun PriorityCard(assessment: MaintenanceAssessment, bike: Bike, onLogService: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Column(Modifier.weight(1f).widthIn(min = AppSpacing.xxl * 3), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    StatusBadge(assessment.status)
                    Text(assessment.item.name, style = MaterialTheme.typography.headlineMedium)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) {
                    Icon(painterResource(R.drawable.ic_motorcycle), localizedString(R.string.maintenance_bike_placeholder),
                        Modifier.size(AppSpacing.xxl * 2).padding(AppSpacing.md), tint = MaterialTheme.colorScheme.primary)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                assessment.item.dueDate?.let {
                    Detail(R.string.maintenance_due_date, formatDate(it), Modifier.weight(1f))
                }
                assessment.item.dueOdometerKm?.let {
                    Detail(R.string.maintenance_due_odometer, formatKm(it), Modifier.weight(1f))
                }
                Detail(R.string.maintenance_current_odometer, formatKm(bike.currentOdometer), Modifier.weight(1f))
            }
            HorizontalDivider()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                assessment.remainingDays?.let { remaining ->
                    Text(localizedString(when {
                        remaining < 0 -> R.string.maintenance_days_overdue
                        remaining == 0L -> R.string.maintenance_due_today
                        else -> R.string.maintenance_days_remaining
                    }, formatNumber(abs(remaining))), color = statusColor(assessment.status))
                }
                assessment.remainingKm?.let { remaining ->
                    Text(localizedString(when {
                        remaining < 0 -> R.string.maintenance_km_overdue
                        remaining == 0L -> R.string.maintenance_due_now
                        else -> R.string.maintenance_km_remaining
                    }, formatNumber(abs(remaining))), color = statusColor(assessment.status))
                }
            }
            Text(localizedString(R.string.maintenance_progress), style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(progress = { assessment.dueWindowProgress }, modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.action, trackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            Text(localizedString(R.string.maintenance_progress_help, formatNumber(DUE_SOON_DAYS), formatNumber(DUE_SOON_KM)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BikeCarePrimaryButton(localizedString(R.string.maintenance_log_service), onLogService,
                leadingContent = { Icon(painterResource(R.drawable.ic_care), null); Spacer(Modifier.width(AppSpacing.xs)) })
        }
    }
}

@Composable
private fun MaintenanceRow(assessment: MaintenanceAssessment, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Icon(painterResource(R.drawable.ic_care), null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(assessment.item.name, style = MaterialTheme.typography.titleMedium)
                StatusBadge(assessment.status)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    assessment.item.dueDate?.let {
                        Detail(R.string.maintenance_due_date, formatDate(it), Modifier.weight(1f))
                    }
                    assessment.item.dueOdometerKm?.let {
                        Detail(R.string.maintenance_due_odometer, formatKm(it), Modifier.weight(1f))
                    }
                }
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Detail(label: Int, value: String, modifier: Modifier) {
    Column(modifier.widthIn(min = AppSpacing.xxl * 2), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        Text(localizedString(label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StatusBadge(status: MaintenanceStatus) {
    val color = statusColor(status)
    Surface(color = color.copy(alpha = 0.1f), contentColor = color, shape = MaterialTheme.shapes.extraLarge) {
        Text(localizedString(statusLabel(status)), Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
            style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MaintenanceMessage(message: String, isError: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(message, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private fun statusLabel(status: MaintenanceStatus): Int = when (status) {
    MaintenanceStatus.OVERDUE -> R.string.maintenance_overdue
    MaintenanceStatus.DUE_SOON -> R.string.maintenance_due_soon
    MaintenanceStatus.UP_TO_DATE -> R.string.maintenance_up_to_date
}

@Composable
private fun statusColor(status: MaintenanceStatus): Color = when (status) {
    MaintenanceStatus.OVERDUE -> AppTheme.colors.error
    MaintenanceStatus.DUE_SOON -> AppTheme.colors.warning
    MaintenanceStatus.UP_TO_DATE -> AppTheme.colors.success
}

@Composable
private fun formatNumber(value: Long): String = NumberFormat.getIntegerInstance(LocalLocale.current).format(value)

@Composable
private fun formatKm(value: Long): String = localizedString(R.string.maintenance_km, formatNumber(value))

@Composable
private fun formatDate(value: LocalDate): String = value.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocalLocale.current)
        .withDecimalStyle(java.time.format.DecimalStyle.of(LocalLocale.current)),
)

@Preview(widthDp = 412, heightDp = 1100, showBackground = true)
@Composable
private fun MaintenancePreview() = MaintenancePreviewContent(false, AppLanguage.ENGLISH)

@Preview(widthDp = 320, heightDp = 1100, fontScale = 1.5f, showBackground = true)
@Composable
private fun MaintenanceCompactPreview() = MaintenancePreviewContent(true, AppLanguage.BANGLA)

@Composable
private fun MaintenancePreviewContent(dark: Boolean, language: AppLanguage) {
    val bike = Bike("preview-bike", "Yamaha", "FZ-S FI", year = 2024, registrationNumber = "", initialOdometer = 0, currentOdometer = 6_000)
    val today = LocalDate.of(2026, 10, 4)
    val items = listOf(
        MaintenanceItem("oil", bike.id, "Engine oil", today.minusDays(2), 6_500, 0, 0),
        MaintenanceItem("brakes", bike.id, "Brake inspection", today.plusDays(10), 6_800, 0, 0),
        MaintenanceItem("chain", bike.id, "Chain lubrication", today.plusDays(60), 8_000, 0, 0),
    ).map { it.assess(today, bike.currentOdometer) }
    ProvideLocalization(language) {
        BikeCareTheme(darkTheme = dark) {
            Surface(color = MaterialTheme.colorScheme.background) {
                MaintenanceScreen(MaintenanceUiState.Content(bike, items, items.first(), 1, 1, 1), {})
            }
        }
    }
}
