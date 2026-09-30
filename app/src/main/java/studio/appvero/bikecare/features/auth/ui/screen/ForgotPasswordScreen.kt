package studio.appvero.bikecare.features.auth.ui.screen

import android.content.res.Configuration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun ForgotPasswordScreen(uiState: ForgotPasswordUiState, onEvent: (ForgotPasswordEvent) -> Unit) {
    AuthLayout(
        title = localizedString(R.string.auth_reset_title),
        subtitle = localizedString(R.string.auth_reset_subtitle),
        state = uiState.form,
        submitLabel = localizedString(if (uiState.sent) R.string.auth_sign_in else R.string.auth_send_reset_email),
        onSubmit = { onEvent(ForgotPasswordEvent.Submit) },
        footer = {
            TextButton(onClick = { onEvent(ForgotPasswordEvent.Login) }, enabled = !uiState.form.busy) {
                Text(localizedString(R.string.auth_back_to_login))
            }
        },
    ) {
        if (!uiState.sent) EmailField(uiState.form) { onEvent(ForgotPasswordEvent.EmailChanged(it)) }
    }
}

@Preview(name = "Reset light", widthDp = 412)
@Preview(name = "Reset Bangla large dark", widthDp = 412, locale = "bn", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ForgotPasswordPreview() {
    BikeCareTheme { ForgotPasswordScreen(ForgotPasswordUiState(), {}) }
}