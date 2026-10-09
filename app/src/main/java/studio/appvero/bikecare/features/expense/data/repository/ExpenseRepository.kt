package studio.appvero.bikecare.features.expense.data.repository

import kotlinx.coroutines.flow.Flow
import studio.appvero.bikecare.features.expense.domain.model.Expense

interface ExpenseRepository {
    /** Emits the 100 most recent expenses. Local writes may still be rejected by the server. */
    fun observeRecentExpenses(): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
}

enum class ExpenseFailure { Network, PermissionDenied, AuthenticationExpired, InvalidData, Unknown }
class ExpenseException(val failure: ExpenseFailure) : Exception(failure.name)
