package studio.appvero.bikecare.features.expense.ui.screen

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
import studio.appvero.bikecare.core.localization.LocalLocale
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.expense.domain.model.Expense
import studio.appvero.bikecare.features.expense.domain.model.ExpenseCategory
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import java.text.NumberFormat

@Composable
fun ExpenseListScreen(state: ExpenseListUiState, onEvent: (ExpenseListEvent) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        item {
            Text(localizedString(R.string.expense_title), style = MaterialTheme.typography.headlineLarge)
            Text(localizedString(R.string.expense_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { BikeCareButton(localizedString(R.string.expense_add), { onEvent(ExpenseListEvent.AddExpense) }) }
        when {
            state.isLoading -> item {
                Column(Modifier.fillMaxWidth().padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(localizedString(R.string.expense_loading))
                }
            }
            state.error != null -> item {
                ExpenseMessageCard {
                    Text(localizedString(state.error), color = MaterialTheme.colorScheme.error)
                    BikeCareButton(localizedString(R.string.expense_retry), { onEvent(ExpenseListEvent.Retry) }, outlined = true)
                }
            }
            state.expenses.isEmpty() -> item {
                ExpenseMessageCard {
                    Text(localizedString(R.string.expense_empty), style = MaterialTheme.typography.titleLarge)
                    Text(localizedString(R.string.expense_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                items(state.expenses, key = { it.id }) { expense ->
                    ExpenseCard(expense, state.bikeNames[expense.bikeId])
                }
                if (state.expenses.size == 100) item {
                    Text(localizedString(R.string.expense_recent_limit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { BikeCareButton(localizedString(R.string.expense_back), { onEvent(ExpenseListEvent.Back) }, outlined = true) }
    }
}

@Composable
private fun ExpenseMessageCard(content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            content()
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, bikeName: String?) {
    val number = NumberFormat.getIntegerInstance(LocalLocale.current)
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(expense.title, style = MaterialTheme.typography.titleLarge)
            Text(localizedString(R.string.expense_log_amount, number.format(expense.amount)),
                style = MaterialTheme.typography.titleMedium)
            Text(localizedString(R.string.expense_log_category, localizedString(expenseCategoryLabel(expense.category))))
            Text(localizedString(R.string.expense_log_bike, bikeName ?: expense.bikeId))
            Text(localizedString(R.string.expense_log_date, expenseDate(expense.date)))
            if (expense.notes.isNotBlank()) Text(expense.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (expense.isSyncPending) Text(localizedString(R.string.expense_sync_pending),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

internal fun expenseCategoryLabel(category: ExpenseCategory): Int = when (category) {
    ExpenseCategory.ACCESSORY -> R.string.expense_category_accessory
    ExpenseCategory.PARKING -> R.string.expense_category_parking
    ExpenseCategory.TOLL -> R.string.expense_category_toll
    ExpenseCategory.INSURANCE -> R.string.expense_category_insurance
    ExpenseCategory.REGISTRATION -> R.string.expense_category_registration
    ExpenseCategory.OTHER -> R.string.expense_category_other
}
