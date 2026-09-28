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
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.data.repository.RegistrationResult
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()
    // Retain a pending effect across collector recreation until the route acknowledges it.
    private val _sideEffect = MutableSharedFlow<RegisterSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: RegisterEvent) {
        if (event == RegisterEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.form.busy) return
        when (event) {
            is RegisterEvent.EmailChanged -> update { it.copy(email = event.value, emailError = null, error = null, message = null) }
            is RegisterEvent.PasswordChanged -> update { it.copy(password = event.value, passwordError = null, confirmPasswordError = null, error = null) }
            is RegisterEvent.ConfirmPasswordChanged -> update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            RegisterEvent.TogglePasswordVisibility -> update { it.copy(passwordVisible = !it.passwordVisible) }
            RegisterEvent.Submit -> {
                val validated = AuthValidation.validate(_uiState.value.form, registering = true)
                update { validated }
                if (AuthValidation.isValid(validated)) {
                    update { it.copy(isLoading = true) }
                    authenticate { repository.register(validated.email, validated.password) }
                }
            }

            RegisterEvent.Login -> _sideEffect.tryEmit(RegisterSideEffect.NavigateToLogin)
        }
    }

    private fun authenticate(operation: suspend () -> RegistrationResult) {
        viewModelScope.launch {
            try {
                val result = operation()
                // Clear passwords after success; never persist them in saved state.
                update { it.copy(password = "", confirmPassword = "") }
                if (result == RegistrationResult.SignedIn) {
                    _sideEffect.emit(RegisterSideEffect.NavigateToHome)
                } else {
                    update { it.copy(isLoading = false, message = R.string.auth_confirmation_required) }
                }
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
