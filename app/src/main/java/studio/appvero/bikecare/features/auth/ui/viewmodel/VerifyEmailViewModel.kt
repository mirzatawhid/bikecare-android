package studio.appvero.bikecare.features.auth.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import studio.appvero.bikecare.R
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import studio.appvero.bikecare.navigation.VerifyEmailRoute
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val repository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyEmailUiState(email = repository.getCurrentUser()?.email.orEmpty()))
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<VerifyEmailSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var navigationPending = false

    init {
        viewModelScope.launch {
            repository.observeAuthState().collect { user ->
                if (user == null) navigate(VerifyEmailSideEffect.NavigateToLogin)
                else _uiState.update { it.copy(email = user.email.orEmpty()) }
            }
        }
        if (savedStateHandle.toRoute<VerifyEmailRoute>().sendEmail && savedStateHandle.get<Boolean>("deliveryAttempted") != true) {
            savedStateHandle["deliveryAttempted"] = true
            onEvent(VerifyEmailEvent.Resend)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: VerifyEmailEvent) {
        if (event == VerifyEmailEvent.EffectHandled) {
            _sideEffect.resetReplayCache()
            return
        }
        if (_uiState.value.form.busy || navigationPending) return
        _uiState.update { it.copy(form = it.form.copy(isLoading = true, error = null, message = null)) }
        viewModelScope.launch {
            try {
                when (event) {
                    VerifyEmailEvent.Resend -> {
                        repository.sendVerificationEmail()
                        _uiState.update { it.copy(form = it.form.copy(message = R.string.auth_verification_sent)) }
                    }
                    VerifyEmailEvent.Refresh -> {
                        val user = repository.refreshUser()
                        when {
                            user == null -> navigate(VerifyEmailSideEffect.NavigateToLogin)
                            user.isEmailVerified -> navigate(VerifyEmailSideEffect.NavigateToHome)
                            else -> _uiState.update { it.copy(form = it.form.copy(message = R.string.auth_verification_pending)) }
                        }
                    }
                    VerifyEmailEvent.Logout -> {
                        repository.logout()
                        navigate(VerifyEmailSideEffect.NavigateToLogin)
                    }
                    VerifyEmailEvent.EffectHandled -> Unit
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

    private fun navigate(effect: VerifyEmailSideEffect) {
        if (navigationPending) return
        navigationPending = true
        _sideEffect.tryEmit(effect)
    }
}