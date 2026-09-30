package studio.appvero.bikecare.features.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import javax.inject.Inject

/** Session guard shared by every destination in the authenticated shell. */
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<HomeSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var navigationPending = false

    init {
        viewModelScope.launch {
            val initialUid = repository.getCurrentUser()?.uid
            repository.observeAuthState().collect { user ->
                if (!navigationPending && (user == null || !user.isEmailVerified || user.uid != initialUid)) {
                    navigationPending = true
                    _sideEffect.emit(if (user == null || user.uid != initialUid) HomeSideEffect.NavigateToLogin else HomeSideEffect.NavigateToVerifyEmail)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: HomeEvent) {
        if (event == HomeEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.form.busy || navigationPending) return
        _uiState.update { it.copy(form = it.form.copy(isLoading = true, error = null)) }
        viewModelScope.launch {
            try {
                when (event) {
                    HomeEvent.Logout -> repository.logout()
                    HomeEvent.Refresh -> repository.refreshUser()
                    HomeEvent.EffectHandled -> Unit
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(form = it.form.copy(error = authError(error))) }
            } finally {
                _uiState.update { it.copy(form = it.form.copy(isLoading = false)) }
            }
        }
    }
}
