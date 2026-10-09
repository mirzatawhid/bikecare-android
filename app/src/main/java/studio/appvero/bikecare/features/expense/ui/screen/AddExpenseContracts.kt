package studio.appvero.bikecare.features.expense.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.expense.domain.model.ExpenseCategory
import studio.appvero.bikecare.features.garage.domain.model.Bike

enum class ExpensePicker { BIKE, CATEGORY, DATE }

data class AddExpenseUiState(
    val bikes: List<Bike> = emptyList(),
    val bikesLoading: Boolean = true,
    @param:StringRes val bikesError: Int? = null,
    val bikeId: String = "",
    val category: ExpenseCategory? = null,
    val title: String = "",
    val amount: String = "",
    val date: Long = 0,
    val notes: String = "",
    val openPicker: ExpensePicker? = null,
    @param:StringRes val bikeError: Int? = null,
    @param:StringRes val categoryError: Int? = null,
    @param:StringRes val titleError: Int? = null,
    @param:StringRes val amountError: Int? = null,
    @param:StringRes val dateError: Int? = null,
    @param:StringRes val notesError: Int? = null,
    @param:StringRes val error: Int? = null,
    val isSaving: Boolean = false,
)

sealed interface AddExpenseEvent {
    data class BikeSelected(val id: String) : AddExpenseEvent
    data class CategorySelected(val value: ExpenseCategory) : AddExpenseEvent
    data class TitleChanged(val value: String) : AddExpenseEvent
    data class AmountChanged(val value: String) : AddExpenseEvent
    data class DateSelected(val value: Long) : AddExpenseEvent
    data class NotesChanged(val value: String) : AddExpenseEvent
    data class OpenPicker(val value: ExpensePicker) : AddExpenseEvent
    data object ClosePicker : AddExpenseEvent
    data object RetryBikes : AddExpenseEvent
    data object Submit : AddExpenseEvent
    data object Back : AddExpenseEvent
    data object EffectHandled : AddExpenseEvent
}

sealed interface AddExpenseSideEffect {
    data object NavigateBack : AddExpenseSideEffect
}
