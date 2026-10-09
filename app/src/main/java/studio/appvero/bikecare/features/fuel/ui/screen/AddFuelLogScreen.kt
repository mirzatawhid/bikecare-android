package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.components.BikeCareTextField
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing

@Composable
fun AddFuelLogScreen(state: AddFuelLogUiState, onEvent: (AddFuelLogEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(localizedString(R.string.fuel_add), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(R.string.fuel_form_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)

        when {
            state.bikesLoading -> Text(localizedString(R.string.fuel_bikes_loading))
            state.bikesError != null -> {
                Text(localizedString(state.bikesError), color = MaterialTheme.colorScheme.error)
                BikeCareButton(localizedString(R.string.fuel_retry), { onEvent(AddFuelLogEvent.RetryBikes) }, outlined = true)
            }
            state.bikes.isEmpty() -> Text(localizedString(R.string.fuel_no_bikes),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onEvent(AddFuelLogEvent.OpenPicker(FuelPicker.BIKE)) },
                    enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(localizedString(R.string.fuel_bike), style = MaterialTheme.typography.labelMedium)
                        Text(state.bikes.firstOrNull { it.id == state.bikeId }?.let { "${it.brand} ${it.model}" }
                            ?: localizedString(R.string.fuel_choose_bike))
                    }
                }
                DropdownMenu(expanded = state.openPicker == FuelPicker.BIKE,
                    onDismissRequest = { onEvent(AddFuelLogEvent.ClosePicker) }) {
                    state.bikes.forEach { bike ->
                        DropdownMenuItem(text = { Text("${bike.brand} ${bike.model}") },
                            onClick = { onEvent(AddFuelLogEvent.BikeSelected(bike.id)) },
                            modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget))
                    }
                }
            }
        }
        FuelFieldError(state.bikeError)

        OutlinedButton(onClick = { onEvent(AddFuelLogEvent.OpenPicker(FuelPicker.DATE)) },
            enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
            Column(Modifier.fillMaxWidth()) {
                Text(localizedString(R.string.fuel_date), style = MaterialTheme.typography.labelMedium)
                Text(fuelDate(state.date))
            }
        }
        FuelFieldError(state.dateError)

        BikeCareTextField(state.odometer, { onEvent(AddFuelLogEvent.OdometerChanged(it)) },
            localizedString(R.string.fuel_odometer), enabled = !state.isSaving,
            isError = state.odometerError != null, supportingText = state.odometerError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        BikeCareTextField(state.fuelType, { onEvent(AddFuelLogEvent.FuelTypeChanged(it)) },
            localizedString(R.string.fuel_type), enabled = !state.isSaving,
            isError = state.fuelTypeError != null, supportingText = state.fuelTypeError?.let { localizedString(it) })
        BikeCareTextField(state.quantity, { onEvent(AddFuelLogEvent.QuantityChanged(it)) },
            localizedString(R.string.fuel_quantity), enabled = !state.isSaving,
            isError = state.quantityError != null, supportingText = state.quantityError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        BikeCareTextField(state.pricePerLiter, { onEvent(AddFuelLogEvent.PriceChanged(it)) },
            localizedString(R.string.fuel_price), enabled = !state.isSaving,
            isError = state.priceError != null, supportingText = state.priceError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Text(localizedString(R.string.fuel_total, state.totalCost.ifBlank { "—" }),
            style = MaterialTheme.typography.titleMedium)
        BikeCareTextField(state.station, { onEvent(AddFuelLogEvent.StationChanged(it)) },
            localizedString(R.string.fuel_station), enabled = !state.isSaving,
            isError = state.stationError != null, supportingText = state.stationError?.let { localizedString(it) })
        Row(Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget),
            verticalAlignment = Alignment.CenterVertically) {
            Text(localizedString(R.string.fuel_full_tank), modifier = Modifier.weight(1f))
            Switch(checked = state.fullTank, onCheckedChange = { onEvent(AddFuelLogEvent.FullTankChanged(it)) },
                enabled = !state.isSaving)
        }
        BikeCareTextField(state.notes, { onEvent(AddFuelLogEvent.NotesChanged(it)) },
            localizedString(R.string.fuel_notes), enabled = !state.isSaving, singleLine = false,
            isError = state.notesError != null, supportingText = state.notesError?.let { localizedString(it) })
        state.error?.let { Text(localizedString(it), color = MaterialTheme.colorScheme.error) }
        BikeCareButton(localizedString(R.string.fuel_save), { onEvent(AddFuelLogEvent.Submit) },
            enabled = !state.isSaving && !state.bikesLoading && state.bikesError == null && state.bikes.isNotEmpty(),
            isLoading = state.isSaving)
        BikeCareButton(localizedString(R.string.fuel_back_to_list), { onEvent(AddFuelLogEvent.Back) },
            enabled = !state.isSaving, outlined = true)
    }

    if (state.openPicker == FuelPicker.DATE) FuelDateDialog(state.date,
        onConfirm = { onEvent(AddFuelLogEvent.DateSelected(it)) },
        onDismiss = { onEvent(AddFuelLogEvent.ClosePicker) })
}

@Composable
private fun FuelDateDialog(initialDate: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    key(initialDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        DatePickerDialog(onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { picker.selectedDateMillis?.let(onConfirm) ?: onDismiss() }) {
                    Text(localizedString(R.string.fuel_date_confirm))
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss) { Text(localizedString(R.string.fuel_date_cancel)) }
            }) { DatePicker(state = picker) }
    }
}

@Composable
private fun FuelFieldError(@androidx.annotation.StringRes error: Int?) {
    if (error != null) Text(localizedString(error), color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall)
}
