package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.runtime.Composable
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString

/** Temporary Home content until the bike dashboard is implemented. */
@Composable
fun AuthSuccessScreen(uiState: HomeUiState, onEvent: (HomeEvent) -> Unit) {
    AuthLayout(
        title = localizedString(R.string.auth_success_title),
        subtitle = localizedString(R.string.auth_success_body),
        state = uiState.form,
        submitLabel = localizedString(R.string.auth_sign_out),
        onSubmit = { onEvent(HomeEvent.Logout) },
        footer = {},
        fields = {},
    )
}