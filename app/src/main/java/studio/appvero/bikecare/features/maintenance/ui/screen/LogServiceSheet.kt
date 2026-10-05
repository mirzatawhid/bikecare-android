package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.core.localization.LocalLocalizedContext
import studio.appvero.bikecare.core.localization.LocalLocale
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField.*
import studio.appvero.bikecare.ui.components.*
import studio.appvero.bikecare.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogServiceSheet(form: LogServiceFormState, onEvent: (MaintenanceEvent) -> Unit) {
    val isSaving = rememberUpdatedState(form.isSaving)
    val confirmValueChange = remember(form.id) {
        { value: SheetValue -> value != SheetValue.Hidden || !isSaving.value }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange)
    val focus = LocalFocusManager.current
    var selectingService by remember { mutableStateOf(false) }
    var dateField by remember { mutableStateOf<ServiceField?>(null) }
    ModalBottomSheet(
        onDismissRequest = { onEvent(MaintenanceEvent.DismissLogService) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(start = AppDimensions.screenHorizontalPadding, end = AppDimensions.screenHorizontalPadding,
                    bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(localizedString(R.string.service_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                BikeCareIconButton(localizedString(R.string.garage_close_sheet),
                    { onEvent(MaintenanceEvent.DismissLogService) }, enabled = !form.isSaving) {
                    Text("×", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Text(localizedString(R.string.service_subtitle, form.bike.nickname ?: "${form.bike.brand} ${form.bike.model}"),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (form.services.isNotEmpty()) {
                Box {
                    TextButton(onClick = { selectingService = true }, enabled = !form.isSaving) {
                        Text(localizedString(R.string.service_choose))
                    }
                    DropdownMenu(expanded = selectingService, onDismissRequest = { selectingService = false }) {
                        DropdownMenuItem(text = { Text(localizedString(R.string.service_other)) }, onClick = {
                            selectingService = false; onEvent(MaintenanceEvent.SelectService(null))
                        })
                        form.services.forEach { item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = {
                                selectingService = false; onEvent(MaintenanceEvent.SelectService(item.id))
                            })
                        }
                    }
                }
            }
            ServiceInput(form, Name, R.string.service_name, onEvent, required = true)
            ServiceFieldPair(
                first = { ServiceDateInput(form, ServiceDate, R.string.service_date) { dateField = ServiceDate } },
                second = { ServiceInput(form, Odometer, R.string.service_odometer, onEvent, KeyboardType.Number, true) },
            )
            ServiceFieldPair(
                first = { ServiceInput(form, Cost, R.string.service_cost, onEvent, KeyboardType.Decimal) },
                second = { ServiceInput(form, Workshop, R.string.service_workshop, onEvent) },
            )
            ServiceInput(form, Notes, R.string.service_notes, onEvent)
            Row(Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)
                .toggleable(form.reminderEnabled, enabled = !form.isSaving, role = Role.Checkbox,
                    onValueChange = { onEvent(MaintenanceEvent.SetReminder(it)) }),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(form.reminderEnabled, onCheckedChange = null, enabled = !form.isSaving,
                    colors = CheckboxDefaults.colors(checkedColor = AppTheme.colors.action, checkmarkColor = AppTheme.colors.onAction))
                Spacer(Modifier.width(AppSpacing.xs))
                Text(localizedString(R.string.service_set_reminder))
            }
            AnimatedVisibility(form.reminderEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text(localizedString(R.string.service_due_section), style = MaterialTheme.typography.titleMedium)
                    Text(localizedString(R.string.service_due_help), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ServiceFieldPair(
                        first = { ServiceInput(form, DueOdometer, R.string.service_due_km, onEvent, KeyboardType.Number) },
                        second = { ServiceDateInput(form, DueDate, R.string.service_due_date) { dateField = DueDate } },
                    )
                    form.errors[ReminderDue]?.let {
                        Text(localizedString(it), color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    Text(localizedString(R.string.service_repeat_section), style = MaterialTheme.typography.titleMedium)
                    Text(localizedString(R.string.service_repeat_help), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ServiceFieldPair(
                        first = { ServiceInput(form, RepeatKm, R.string.service_repeat_km, onEvent, KeyboardType.Number) },
                        second = { ServiceInput(form, RepeatDays, R.string.service_repeat_days, onEvent, KeyboardType.Number) },
                    )
                }
            }
            form.submissionError?.let {
                Text(localizedString(it), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            BikeCarePrimaryButton(localizedString(R.string.service_save),
                { focus.clearFocus(); onEvent(MaintenanceEvent.SaveService) }, isLoading = form.isSaving,
                leadingContent = { Text("✓"); Spacer(Modifier.width(AppSpacing.xs)) })
        }
    }
    val localizedContext = LocalLocalizedContext.current
    CompositionLocalProvider(LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalResources provides localizedContext.resources) {
      dateField?.let { field ->
        val initial = runCatching { LocalDate.parse(form[field]).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull()
        val picker = rememberDatePickerState(initialSelectedDateMillis = initial, yearRange = 1900..9999)
        DatePickerDialog(onDismissRequest = { dateField = null }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let {
                    onEvent(MaintenanceEvent.ServiceFieldChanged(field, Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toString()))
                }
                dateField = null
            }) { Text(localizedString(R.string.service_date_confirm)) }
        }, dismissButton = {
            Row {
                if (field == DueDate) TextButton(onClick = {
                    onEvent(MaintenanceEvent.ServiceFieldChanged(field, "")); dateField = null
                }) { Text(localizedString(R.string.service_date_clear)) }
                TextButton(onClick = { dateField = null }) { Text(localizedString(R.string.service_date_cancel)) }
            }
        }) { DatePicker(picker) }
      }
    }
}

@Composable
private fun ServiceInput(form: LogServiceFormState, field: ServiceField, label: Int,
    onEvent: (MaintenanceEvent) -> Unit, keyboard: KeyboardType = KeyboardType.Text, required: Boolean = false) {
    BikeCareTextField(form[field], { onEvent(MaintenanceEvent.ServiceFieldChanged(field, it)) }, localizedString(label),
        required = required, error = form.errors[field]?.let { localizedString(it) }, enabled = !form.isSaving,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), singleLine = field != Notes,
        minLines = if (field == Notes) 3 else 1)
}

@Composable
private fun ServiceDateInput(form: LogServiceFormState, field: ServiceField, label: Int, onClick: () -> Unit) {
    val locale = LocalLocale.current
    val display = runCatching { LocalDate.parse(form[field]).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)) }
        .getOrDefault(form[field])
    BikeCareTextField(display, {}, localizedString(label), required = field == ServiceDate,
        enabled = !form.isSaving, readOnly = true, error = form.errors[field]?.let { localizedString(it) },
        trailingIcon = {
            BikeCareIconButton(localizedString(label), onClick, enabled = !form.isSaving) {
                Icon(painterResource(R.drawable.ic_service_calendar), contentDescription = null)
            }
        })
}

@Composable
private fun ServiceFieldPair(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 320.dp || fontScale > 1.3f) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) { first(); second() }
        } else Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Box(Modifier.weight(1f)) { first() }
            Box(Modifier.weight(1f)) { second() }
        }
    }
}
