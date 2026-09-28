package studio.appvero.bikecare.features.auth.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun RegisterScreen(uiState: RegisterUiState, onEvent: (RegisterEvent) -> Unit) {
    val form = uiState.form
    AuthLayout(
        title = localizedString(R.string.auth_register_title),
        subtitle = localizedString(R.string.auth_register_subtitle),
        state = form,
        submitLabel = localizedString(R.string.auth_create_account),
        onSubmit = { onEvent(RegisterEvent.Submit) },
        footer = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(localizedString(R.string.auth_have_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onEvent(RegisterEvent.Login) }, enabled = !form.busy) {
                    Text(localizedString(R.string.auth_sign_in))
                }
            }
        },
    ) {
        EmailField(form) { onEvent(RegisterEvent.EmailChanged(it)) }
        PasswordField(
            value = form.password, onChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
            state = form, onToggleVisibility = { onEvent(RegisterEvent.TogglePasswordVisibility) },
            error = form.passwordError, registering = true, lastField = false,
            onSubmit = { onEvent(RegisterEvent.Submit) },
        )
        PasswordField(
            value = form.confirmPassword, onChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
            state = form, onToggleVisibility = { onEvent(RegisterEvent.TogglePasswordVisibility) },
            error = form.confirmPasswordError, confirm = true, registering = true,
            onSubmit = { onEvent(RegisterEvent.Submit) },
        )
    }
}

@Preview(name = "Register light", widthDp = 412, showBackground = true)
@Preview(name = "Register dark", widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Bangla large text", widthDp = 412, locale = "bn", fontScale = 1.5f)
@Composable
private fun RegisterPreview() {
    BikeCareTheme { RegisterScreen(RegisterUiState(), {}) }
}

@Preview(name = "Register error", widthDp = 412, showBackground = true)
@Composable
private fun RegisterErrorPreview() {
    BikeCareTheme { RegisterScreen(RegisterUiState(AuthFormState(error = R.string.auth_network_error)), {}) }
}
