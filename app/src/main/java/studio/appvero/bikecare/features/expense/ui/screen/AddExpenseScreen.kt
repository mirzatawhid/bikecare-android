package studio.appvero.bikecare.features.expense.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.expense.domain.model.ExpenseCategory
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.components.BikeCareTextField
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing

@Composable
fun AddExpenseScreen(state: AddExpenseUiState, onEvent: (AddExpenseEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(localizedString(R.string.expense_add), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(R.string.expense_form_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)

        when {
            state.bikesLoading -> Text(localizedString(R.string.expense_bikes_loading))
            state.bikesError != null -> {
                Text(localizedString(state.bikesError), color = MaterialTheme.colorScheme.error)
                BikeCareButton(localizedString(R.string.expense_retry), { onEvent(AddExpenseEvent.RetryBikes) }, outlined = true)
            }
            state.bikes.isEmpty() -> Text(localizedString(R.string.expense_no_bikes),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onEvent(AddExpenseEvent.OpenPicker(ExpensePicker.BIKE)) },
                    enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(localizedString(R.string.expense_bike), style = MaterialTheme.typography.labelMedium)
                        Text(state.bikes.firstOrNull { it.id == state.bikeId }?.let { "${it.brand} ${it.model}" }
                            ?: localizedString(R.string.expense_choose_bike))
                    }
                }
                DropdownMenu(expanded = state.openPicker == ExpensePicker.BIKE,
                    onDismissRequest = { onEvent(AddExpenseEvent.ClosePicker) }) {
                    state.bikes.forEach { bike ->
                        DropdownMenuItem(text = { Text("${bike.brand} ${bike.model}") },
                            onClick = { onEvent(AddExpenseEvent.BikeSelected(bike.id)) },
                            modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget))
                    }
                }
            }
        }
        ExpenseFieldError(state.bikeError)

        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onEvent(AddExpenseEvent.OpenPicker(ExpensePicker.CATEGORY)) },
                enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
                Column(Modifier.fillMaxWidth()) {
                    Text(localizedString(R.string.expense_category), style = MaterialTheme.typography.labelMedium)
                    Text(state.category?.let { localizedString(expenseCategoryLabel(it)) }
                        ?: localizedString(R.string.expense_choose_category))
                }
            }
            DropdownMenu(expanded = state.openPicker == ExpensePicker.CATEGORY,
                onDismissRequest = { onEvent(AddExpenseEvent.ClosePicker) }) {
                ExpenseCategory.entries.forEach { category ->
                    DropdownMenuItem(text = { Text(localizedString(expenseCategoryLabel(category))) },
                        onClick = { onEvent(AddExpenseEvent.CategorySelected(category)) },
                        modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget))
                }
            }
        }
        ExpenseFieldError(state.categoryError)

        BikeCareTextField(state.title, { onEvent(AddExpenseEvent.TitleChanged(it)) },
            localizedString(R.string.expense_field_title), enabled = !state.isSaving,
            isError = state.titleError != null, supportingText = state.titleError?.let { localizedString(it) })
        BikeCareTextField(state.amount, { onEvent(AddExpenseEvent.AmountChanged(it)) },
            localizedString(R.string.expense_field_amount), enabled = !state.isSaving,
            isError = state.amountError != null, supportingText = state.amountError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        OutlinedButton(onClick = { onEvent(AddExpenseEvent.OpenPicker(ExpensePicker.DATE)) },
            enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)) {
            Column(Modifier.fillMaxWidth()) {
                Text(localizedString(R.string.expense_date), style = MaterialTheme.typography.labelMedium)
                Text(expenseDate(state.date))
            }
        }
        ExpenseFieldError(state.dateError)

        BikeCareTextField(state.notes, { onEvent(AddExpenseEvent.NotesChanged(it)) },
            localizedString(R.string.expense_notes), enabled = !state.isSaving, singleLine = false,
            isError = state.notesError != null, supportingText = state.notesError?.let { localizedString(it) })
        state.error?.let { Text(localizedString(it), color = MaterialTheme.colorScheme.error) }
        BikeCareButton(localizedString(R.string.expense_save), { onEvent(AddExpenseEvent.Submit) },
            enabled = !state.isSaving && !state.bikesLoading && state.bikesError == null && state.bikes.isNotEmpty(),
            isLoading = state.isSaving)
        BikeCareButton(localizedString(R.string.expense_back_to_list), { onEvent(AddExpenseEvent.Back) },
            enabled = !state.isSaving, outlined = true)
    }

    if (state.openPicker == ExpensePicker.DATE) key(state.date) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(onDismissRequest = { onEvent(AddExpenseEvent.ClosePicker) },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { onEvent(AddExpenseEvent.DateSelected(it)) }
                        ?: onEvent(AddExpenseEvent.ClosePicker)
                }) { Text(localizedString(R.string.expense_date_confirm)) }
            }, dismissButton = {
                TextButton(onClick = { onEvent(AddExpenseEvent.ClosePicker) }) {
                    Text(localizedString(R.string.expense_date_cancel))
                }
            }) { DatePicker(state = picker) }
    }
}

@Composable
private fun ExpenseFieldError(@androidx.annotation.StringRes error: Int?) {
    if (error != null) Text(localizedString(error), color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall)
}
