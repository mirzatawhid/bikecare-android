package studio.appvero.bikecare.features.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<ForgotPasswordSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: ForgotPasswordEvent) {
        if (event == ForgotPasswordEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.form.busy) return
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> _uiState.update {
                it.copy(form = it.form.copy(email = event.value, emailError = null, error = null))
            }
            ForgotPasswordEvent.Login -> _sideEffect.tryEmit(ForgotPasswordSideEffect.NavigateToLogin)
            ForgotPasswordEvent.Submit -> submit()
            ForgotPasswordEvent.EffectHandled -> Unit
        }
    }

    private fun submit() {
        if (_uiState.value.sent) {
            _sideEffect.tryEmit(ForgotPasswordSideEffect.NavigateToLogin)
            return
        }
        val form = _uiState.value.form
        val error = AuthValidation.emailError(form.email)
        _uiState.update { it.copy(form = form.copy(emailError = error, error = null)) }
        if (error != null) return
        _uiState.update { it.copy(form = it.form.copy(isLoading = true)) }
        viewModelScope.launch {
            try {
                repository.resetPassword(form.email)
                _uiState.update { it.copy(sent = true, form = it.form.copy(message = R.string.auth_reset_sent)) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                _uiState.update { it.copy(form = it.form.copy(error = authError(failure))) }
            } finally {
                _uiState.update { it.copy(form = it.form.copy(isLoading = false)) }
            }
        }
    }
}