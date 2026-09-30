package studio.appvero.bikecare.features.auth.ui.viewmodel

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.appvero.bikecare.core.localization.LanguageManager
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.*
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val languageManager: LanguageManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<SplashSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var startupJob: Job? = null
    private var navigationPending = false

    init { checkAuthentication() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.EffectHandled -> _sideEffect.resetReplayCache()
            SplashEvent.CheckAuthentication, SplashEvent.Retry -> checkAuthentication()
        }
    }

    private fun checkAuthentication() {
        if (startupJob?.isActive == true || navigationPending) return
        startupJob = viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            try {
                languageManager.initialize()
                val user = authRepository.observeAuthState().first()
                navigationPending = true
                _sideEffect.emit(when {
                    user == null -> SplashSideEffect.NavigateToLogin
                    !user.isEmailVerified -> SplashSideEffect.NavigateToVerifyEmail
                    else -> SplashSideEffect.NavigateToHome
                })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = SplashUiState.Error(authError(error))
            }
        }
    }
}
