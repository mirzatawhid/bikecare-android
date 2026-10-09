package studio.appvero.bikecare.features.expense.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.expense.domain.model.Expense

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val bikeNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    @param:StringRes val error: Int? = null,
)

sealed interface ExpenseListEvent {
    data object Retry : ExpenseListEvent
    data object AddExpense : ExpenseListEvent
    data object Back : ExpenseListEvent
    data object EffectHandled : ExpenseListEvent
}

sealed interface ExpenseListSideEffect {
    data object NavigateToAddExpense : ExpenseListSideEffect
    data object NavigateBack : ExpenseListSideEffect
}
