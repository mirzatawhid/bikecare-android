package studio.appvero.bikecare.features.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    // Retain a pending effect across collector recreation until the route acknowledges it.
    private val _sideEffect = MutableSharedFlow<LoginSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: LoginEvent) {
        if (event == LoginEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.form.busy) return
        when (event) {
            is LoginEvent.EmailChanged -> update { it.copy(email = event.value, emailError = null, error = null, message = null) }
            is LoginEvent.PasswordChanged -> update { it.copy(password = event.value, passwordError = null, confirmPasswordError = null, error = null) }
            LoginEvent.TogglePasswordVisibility -> update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginEvent.Submit -> {
                val validated = AuthValidation.validate(_uiState.value.form, registering = false)
                update { validated }
                if (AuthValidation.isValid(validated)) {
                    update { it.copy(isLoading = true) }
                    authenticate { repository.login(validated.email, validated.password) }
                }
            }

            LoginEvent.Register -> _sideEffect.tryEmit(LoginSideEffect.NavigateToRegister)
            LoginEvent.ResetPassword -> _sideEffect.tryEmit(LoginSideEffect.NavigateToResetPassword)
        }
    }

    private fun authenticate(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                // Clear passwords after success; never persist them in saved state.
                update { it.copy(password = "", confirmPassword = "") }
                _sideEffect.emit(LoginSideEffect.NavigateToHome)
            } catch (cancelled: CancellationException) {
                update { it.copy(isLoading = false) }
                throw cancelled
            } catch (error: Exception) {
                update { it.copy(isLoading = false, error = authError(error)) }
            }
        }
    }

    private fun update(transform: (AuthFormState) -> AuthFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }
}
