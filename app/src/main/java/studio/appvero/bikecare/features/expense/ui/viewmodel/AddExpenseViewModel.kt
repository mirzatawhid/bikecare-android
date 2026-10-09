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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.expense.data.repository.ExpenseRepository
import studio.appvero.bikecare.features.expense.domain.model.Expense
import studio.appvero.bikecare.features.expense.ui.screen.AddExpenseEvent
import studio.appvero.bikecare.features.expense.ui.screen.AddExpenseSideEffect
import studio.appvero.bikecare.features.expense.ui.screen.AddExpenseUiState
import studio.appvero.bikecare.features.garage.data.repository.BikeException
import studio.appvero.bikecare.features.garage.data.repository.BikeFailure
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val bikes: BikeRepository,
    private val expenses: ExpenseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseUiState(
        date = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    ))
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<AddExpenseSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var bikesJob: Job? = null

    init { loadBikes() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: AddExpenseEvent) {
        if (event == AddExpenseEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.isSaving) return
        when (event) {
            is AddExpenseEvent.BikeSelected -> _uiState.update { it.copy(bikeId = event.id, bikeError = null, openPicker = null) }
            is AddExpenseEvent.CategorySelected -> _uiState.update { it.copy(category = event.value, categoryError = null, openPicker = null) }
            is AddExpenseEvent.TitleChanged -> _uiState.update { it.copy(title = event.value, titleError = null) }
            is AddExpenseEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value, amountError = null) }
            is AddExpenseEvent.DateSelected -> _uiState.update { it.copy(date = event.value, dateError = null, openPicker = null) }
            is AddExpenseEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value, notesError = null) }
            is AddExpenseEvent.OpenPicker -> _uiState.update { it.copy(openPicker = event.value) }
            AddExpenseEvent.ClosePicker -> _uiState.update { it.copy(openPicker = null) }
            AddExpenseEvent.RetryBikes -> loadBikes()
            AddExpenseEvent.Submit -> submit()
            AddExpenseEvent.Back -> _sideEffect.tryEmit(AddExpenseSideEffect.NavigateBack)
            AddExpenseEvent.EffectHandled -> Unit
        }
    }

    private fun loadBikes() {
        if (bikesJob?.isActive == true) return
        _uiState.update { it.copy(bikesLoading = true, bikesError = null) }
        bikesJob = viewModelScope.launch {
            try {
                bikes.observeUserBikes().collect { items ->
                    val active = items.filter { it.isActive }
                    _uiState.update { current -> current.copy(bikes = active,
                        bikeId = current.bikeId.takeIf { id -> active.any { it.id == id } } ?: "",
                        bikesLoading = false, bikesError = null) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(bikesLoading = false, bikesError = when ((error as? BikeException)?.failure) {
                    BikeFailure.Network -> R.string.expense_network_error
                    BikeFailure.PermissionDenied -> R.string.expense_permission_error
                    BikeFailure.AuthenticationExpired -> R.string.expense_auth_error
                    else -> R.string.expense_bikes_error
                }) }
            }
        }
    }

    private fun submit() {
        val form = _uiState.value
        val amount = form.amount.trim().map { char ->
            val digit = Character.digit(char, 10)
            if (digit >= 0) ('0'.code + digit).toChar() else char
        }.joinToString("").toLongOrNull()
        val bikeError = if (form.bikes.none { it.id == form.bikeId }) R.string.expense_choose_bike else null
        val categoryError = if (form.category == null) R.string.expense_choose_category else null
        val titleError = if (form.title.trim().length !in 1..100) R.string.expense_title_invalid else null
        val amountError = if (amount == null || amount !in 1..1_000_000_000) R.string.expense_amount_invalid else null
        val dateError = if (form.date !in 0..4102444800000L) R.string.expense_date_invalid else null
        val notesError = if (form.notes.trim().length > 1000) R.string.expense_notes_invalid else null
        _uiState.update { it.copy(bikeError = bikeError, categoryError = categoryError,
            titleError = titleError, amountError = amountError, dateError = dateError,
            notesError = notesError, error = null) }
        if (listOf(bikeError, categoryError, titleError, amountError, dateError, notesError).any { it != null }) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                expenses.addExpense(Expense(id = UUID.randomUUID().toString(), bikeId = form.bikeId,
                    category = form.category!!, title = form.title.trim(), amount = amount!!,
                    date = form.date, notes = form.notes.trim()))
                _sideEffect.emit(AddExpenseSideEffect.NavigateBack)
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isSaving = false) }
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(isSaving = false, error = expenseError(error)) }
            }
        }
    }
}
