package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceCategory
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceServiceType
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.components.BikeCareTextField
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing

@Composable
fun AddMaintenanceScreen(state: AddMaintenanceUiState, onEvent: (AddMaintenanceEvent) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(localizedString(R.string.maintenance_add), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(R.string.maintenance_form_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)

        when {
            state.bikesLoading -> Text(localizedString(R.string.maintenance_bikes_loading))
            state.bikesError != null -> {
                Text(localizedString(state.bikesError), color = MaterialTheme.colorScheme.error)
                BikeCareButton(localizedString(R.string.maintenance_retry),
                    onClick = { onEvent(AddMaintenanceEvent.RetryBikes) }, outlined = true)
            }
            state.bikes.isEmpty() -> Text(localizedString(R.string.maintenance_no_bikes),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> {
                ChoiceField(
                    label = localizedString(R.string.maintenance_bike),
                    value = state.bikes.firstOrNull { it.id == state.bikeId }?.let { "${it.brand} ${it.model}" }
                        ?: localizedString(R.string.maintenance_choose_bike),
                    expanded = state.openPicker == MaintenancePicker.BIKE,
                    choices = state.bikes.map { it.id to "${it.brand} ${it.model}" },
                    enabled = !state.isSaving,
                    onOpen = { onEvent(AddMaintenanceEvent.OpenPicker(MaintenancePicker.BIKE)) },
                    onClose = { onEvent(AddMaintenanceEvent.ClosePicker) },
                    onSelect = { onEvent(AddMaintenanceEvent.BikeSelected(it)) },
                )
            }
        }
        FieldError(state.bikeError)

        BikeCareTextField(state.title, { onEvent(AddMaintenanceEvent.TitleChanged(it)) },
            localizedString(R.string.maintenance_field_title), enabled = !state.isSaving,
            isError = state.titleError != null, supportingText = state.titleError?.let { localizedString(it) })

        ChoiceField(localizedString(R.string.maintenance_category), localizedString(state.category.label()),
            expanded = state.openPicker == MaintenancePicker.CATEGORY,
            choices = MaintenanceCategory.entries.map { it to localizedString(it.label()) },
            enabled = !state.isSaving,
            onOpen = { onEvent(AddMaintenanceEvent.OpenPicker(MaintenancePicker.CATEGORY)) },
            onClose = { onEvent(AddMaintenanceEvent.ClosePicker) },
            onSelect = { onEvent(AddMaintenanceEvent.CategorySelected(it)) })

        ChoiceField(localizedString(R.string.maintenance_service_type), localizedString(state.serviceType.label()),
            expanded = state.openPicker == MaintenancePicker.SERVICE_TYPE,
            choices = MaintenanceServiceType.entries.map { it to localizedString(it.label()) },
            enabled = !state.isSaving,
            onOpen = { onEvent(AddMaintenanceEvent.OpenPicker(MaintenancePicker.SERVICE_TYPE)) },
            onClose = { onEvent(AddMaintenanceEvent.ClosePicker) },
            onSelect = { onEvent(AddMaintenanceEvent.ServiceTypeSelected(it)) })

        DateField(localizedString(R.string.maintenance_field_date), maintenanceDate(state.date), !state.isSaving) {
            onEvent(AddMaintenanceEvent.OpenPicker(MaintenancePicker.DATE))
        }
        FieldError(state.dateError)

        BikeCareTextField(state.odometer, { onEvent(AddMaintenanceEvent.OdometerChanged(it)) },
            localizedString(R.string.maintenance_field_odometer), enabled = !state.isSaving,
            isError = state.odometerError != null, supportingText = state.odometerError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        BikeCareTextField(state.cost, { onEvent(AddMaintenanceEvent.CostChanged(it)) },
            localizedString(R.string.maintenance_field_cost), enabled = !state.isSaving,
            isError = state.costError != null, supportingText = state.costError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        BikeCareTextField(state.provider, { onEvent(AddMaintenanceEvent.ProviderChanged(it)) },
            localizedString(R.string.maintenance_field_provider), enabled = !state.isSaving,
            isError = state.providerError != null, supportingText = state.providerError?.let { localizedString(it) })
        BikeCareTextField(state.description, { onEvent(AddMaintenanceEvent.DescriptionChanged(it)) },
            localizedString(R.string.maintenance_field_description), enabled = !state.isSaving,
            singleLine = false, isError = state.descriptionError != null,
            supportingText = state.descriptionError?.let { localizedString(it) })
        BikeCareTextField(state.nextServiceOdometer, { onEvent(AddMaintenanceEvent.NextOdometerChanged(it)) },
            localizedString(R.string.maintenance_field_next_odometer), enabled = !state.isSaving,
            isError = state.nextOdometerError != null,
            supportingText = state.nextOdometerError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        DateField(localizedString(R.string.maintenance_field_next_date),
            state.nextServiceDate?.let { maintenanceDate(it) } ?: localizedString(R.string.maintenance_optional),
            !state.isSaving) { onEvent(AddMaintenanceEvent.OpenPicker(MaintenancePicker.NEXT_DATE)) }
        if (state.nextServiceDate != null) {
            TextButton(onClick = { onEvent(AddMaintenanceEvent.NextDateSelected(null)) }, enabled = !state.isSaving) {
                Text(localizedString(R.string.maintenance_clear_date))
            }
        }
        FieldError(state.nextDateError)
        state.error?.let { Text(localizedString(it), color = MaterialTheme.colorScheme.error) }
        BikeCareButton(localizedString(R.string.maintenance_save),
            onClick = { onEvent(AddMaintenanceEvent.Submit) },
            enabled = !state.isSaving && !state.bikesLoading && state.bikesError == null && state.bikes.isNotEmpty(),
            isLoading = state.isSaving)
        BikeCareButton(localizedString(R.string.maintenance_back),
            onClick = { onEvent(AddMaintenanceEvent.Back) }, enabled = !state.isSaving, outlined = true)
    }

    if (state.openPicker == MaintenancePicker.DATE || state.openPicker == MaintenancePicker.NEXT_DATE) {
        val pickingNext = state.openPicker == MaintenancePicker.NEXT_DATE
        DateSelectionDialog(
            initialDate = if (pickingNext) state.nextServiceDate else state.date,
            onConfirm = { selected ->
                if (pickingNext) onEvent(AddMaintenanceEvent.NextDateSelected(selected))
                else onEvent(AddMaintenanceEvent.DateSelected(selected))
            },
            onDismiss = { onEvent(AddMaintenanceEvent.ClosePicker) },
        )
    }
}

@Composable
private fun <T> ChoiceField(
    label: String,
    value: String,
    expanded: Boolean,
    choices: List<Pair<T, String>>,
    enabled: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onSelect: (T) -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onOpen, enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
            Column(Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onClose) {
            choices.forEach { (choice, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(choice) },
                    modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget))
            }
        }
    }
}

@Composable
private fun DateField(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
        Column(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectionDialog(initialDate: Long?, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    key(initialDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        DatePickerDialog(onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { picker.selectedDateMillis?.let(onConfirm) ?: onDismiss() }) {
                    Text(localizedString(R.string.maintenance_date_confirm))
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss) { Text(localizedString(R.string.maintenance_date_cancel)) }
            }) {
            DatePicker(state = picker)
        }
    }
}

@Composable
private fun FieldError(@androidx.annotation.StringRes error: Int?) {
    if (error != null) Text(localizedString(error), color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall)
}
