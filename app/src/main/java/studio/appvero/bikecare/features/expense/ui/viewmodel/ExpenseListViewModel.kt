package studio.appvero.bikecare.features.expense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.expense.data.repository.ExpenseException
import studio.appvero.bikecare.features.expense.data.repository.ExpenseFailure
import studio.appvero.bikecare.features.expense.data.repository.ExpenseRepository
import studio.appvero.bikecare.features.expense.ui.screen.ExpenseListEvent
import studio.appvero.bikecare.features.expense.ui.screen.ExpenseListSideEffect
import studio.appvero.bikecare.features.expense.ui.screen.ExpenseListUiState
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val bikes: BikeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<ExpenseListSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var loadJob: Job? = null

    init { loadExpenses() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: ExpenseListEvent) {
        when (event) {
            ExpenseListEvent.Retry -> loadExpenses()
            ExpenseListEvent.AddExpense -> _sideEffect.tryEmit(ExpenseListSideEffect.NavigateToAddExpense)
            ExpenseListEvent.Back -> _sideEffect.tryEmit(ExpenseListSideEffect.NavigateBack)
            ExpenseListEvent.EffectHandled -> _sideEffect.resetReplayCache()
        }
    }

    private fun loadExpenses() {
        if (loadJob?.isActive == true) return
        _uiState.value = ExpenseListUiState()
        loadJob = viewModelScope.launch {
            try {
                combine(repository.observeRecentExpenses(), bikes.observeUserBikes()) { expenses, bikeList ->
                    ExpenseListUiState(expenses = expenses,
                        bikeNames = bikeList.associate { it.id to "${it.brand} ${it.model}" }, isLoading = false)
                }.collect { _uiState.value = it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = ExpenseListUiState(isLoading = false, error = expenseError(error))
            }
        }
    }
}

internal fun expenseError(error: Exception): Int = when ((error as? ExpenseException)?.failure) {
    ExpenseFailure.Network -> R.string.expense_network_error
    ExpenseFailure.PermissionDenied -> R.string.expense_permission_error
    ExpenseFailure.AuthenticationExpired -> R.string.expense_auth_error
    ExpenseFailure.InvalidData -> R.string.expense_data_error
    else -> R.string.expense_generic_error
}
