package studio.appvero.bikecare.features.expense.domain.model

/** Dates are epoch milliseconds; amounts are whole BDT. */
data class Expense(
    val id: String,
    val bikeId: String,
    val category: ExpenseCategory,
    val title: String,
    val amount: Long,
    val date: Long,
    val notes: String,
    val createdAt: Long = 0,
    val isSyncPending: Boolean = false,
)

enum class ExpenseCategory { ACCESSORY, PARKING, TOLL, INSURANCE, REGISTRATION, OTHER }
