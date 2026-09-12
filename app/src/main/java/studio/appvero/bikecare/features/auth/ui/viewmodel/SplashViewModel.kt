package studio.appvero.bikecare.features.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import studio.appvero.bikecare.core.localization.LanguageManager
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.ui.screen.SplashEvent
import studio.appvero.bikecare.features.auth.ui.screen.SplashSideEffect
import studio.appvero.bikecare.features.auth.ui.screen.SplashUiState
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(
        SplashUiState.Loading
    )
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SplashSideEffect>(
        extraBufferCapacity = 1,
    )
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        initialize()
        onEvent(SplashEvent.CheckAuthentication)
    }

    fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckAuthentication,
            SplashEvent.Retry -> {
                checkAuthentication()
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            languageManager.initialize()
        }
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            _uiState.update {
                SplashUiState.Loading
            }

            runCatching {
                authRepository.isUserLoggedIn()
            }.onSuccess { isLoggedIn ->

                delay(3000.milliseconds)
                if (isLoggedIn) {
                    emitSideEffect(SplashSideEffect.NavigateToHome)
                } else {
                    emitSideEffect(SplashSideEffect.NavigateToLogin)
                }


            }.onFailure { throwable ->

                _uiState.update {
                    SplashUiState.Error(
                        message = throwable.message
                            ?: "Unable to check authentication."
                    )
                }
            }
        }
    }

    private fun emitSideEffect(effect: SplashSideEffect) {
        viewModelScope.launch {
            _sideEffect.emit(effect)
        }
    }
}