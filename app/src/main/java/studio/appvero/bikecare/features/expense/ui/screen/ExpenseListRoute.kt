package studio.appvero.bikecare.features.expense.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.features.expense.ui.viewmodel.ExpenseListViewModel

@Composable
fun ExpenseListRoute(
    onNavigateToAddExpense: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.sideEffect.collect { effect ->
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            viewModel.onEvent(ExpenseListEvent.EffectHandled)
            when (effect) {
                ExpenseListSideEffect.NavigateToAddExpense -> onNavigateToAddExpense()
                ExpenseListSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    ExpenseListScreen(state, viewModel::onEvent)
}
