package studio.appvero.bikecare.features.auth.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun LoginScreen(uiState: LoginUiState, onEvent: (LoginEvent) -> Unit) {
    val form = uiState.form
    AuthLayout(
        title = localizedString(R.string.auth_login_title),
        subtitle = localizedString(R.string.auth_login_subtitle),
        state = form,
        submitLabel = localizedString(R.string.auth_sign_in),
        onSubmit = { onEvent(LoginEvent.Submit) },
        footer = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(localizedString(R.string.auth_new_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onEvent(LoginEvent.Register) }, enabled = !form.busy) {
                    Text(localizedString(R.string.auth_create_account))
                }
            }
        },
    ) {
        EmailField(form) { onEvent(LoginEvent.EmailChanged(it)) }
        PasswordField(
            value = form.password, onChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            state = form, onToggleVisibility = { onEvent(LoginEvent.TogglePasswordVisibility) },
            error = form.passwordError, registering = false, lastField = true,
            onSubmit = { onEvent(LoginEvent.Submit) },
        )
        TextButton(
            onClick = { onEvent(LoginEvent.ResetPassword) }, enabled = !form.busy,
            modifier = Modifier.align(Alignment.End),
        ) { Text(localizedString(R.string.auth_forgot_password)) }
    }
}

@Preview(name = "Login light", widthDp = 412, showBackground = true)
@Preview(name = "Login dark", widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Bangla large text", widthDp = 412, locale = "bn", fontScale = 1.5f)
@Composable
private fun LoginPreview() {
    BikeCareTheme { LoginScreen(LoginUiState(), {}) }
}

@Preview(name = "Login error", widthDp = 412, showBackground = true)
@Composable
private fun LoginErrorPreview() {
    BikeCareTheme { LoginScreen(LoginUiState(AuthFormState(error = R.string.auth_network_error)), {}) }
}
