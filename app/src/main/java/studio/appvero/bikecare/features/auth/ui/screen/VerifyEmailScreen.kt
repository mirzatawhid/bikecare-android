package studio.appvero.bikecare.features.auth.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun VerifyEmailScreen(uiState: VerifyEmailUiState, onEvent: (VerifyEmailEvent) -> Unit) {
    AuthLayout(
        title = localizedString(R.string.auth_verify_title),
        subtitle = localizedString(R.string.auth_verify_subtitle),
        state = uiState.form,
        submitLabel = localizedString(R.string.auth_verify_refresh),
        onSubmit = { onEvent(VerifyEmailEvent.Refresh) },
        footer = {
            TextButton(onClick = { onEvent(VerifyEmailEvent.Logout) }, enabled = !uiState.form.busy) {
                Text(localizedString(R.string.auth_sign_out))
            }
        },
    ) {
        Text(uiState.email)
        TextButton(
            onClick = { onEvent(VerifyEmailEvent.Resend) }, enabled = !uiState.form.busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget),
        ) { Text(localizedString(R.string.auth_verify_resend)) }
    }
}

@Preview(name = "Verify light", widthDp = 412)
@Preview(name = "Verify Bangla large dark", widthDp = 412, locale = "bn", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerifyEmailPreview() {
    BikeCareTheme { VerifyEmailScreen(VerifyEmailUiState(email = "rider@example.com"), {}) }
}