package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString

@Composable
internal fun EmailField(state: AuthFormState, onChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = state.email, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
        enabled = !state.busy, singleLine = true,
        label = { Text(localizedString(R.string.auth_email)) },
        isError = state.emailError != null,
        supportingText = state.emailError?.let { { Text(localizedString(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
internal fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    state: AuthFormState,
    onToggleVisibility: () -> Unit,
    error: Int?,
    confirm: Boolean = false,
    registering: Boolean = false,
    lastField: Boolean = true,
    onSubmit: () -> Unit,
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().semantics {
            contentType = if (registering) ContentType.NewPassword else ContentType.Password
        },
        enabled = !state.busy, singleLine = true,
        label = { Text(localizedString(if (confirm) R.string.auth_confirm_password else R.string.auth_password)) },
        isError = error != null,
        supportingText = when {
            error != null -> ({ Text(localizedString(error)) })
            registering && !confirm -> ({ Text(localizedString(R.string.auth_password_hint)) })
            else -> null
        },
        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = onToggleVisibility, enabled = !state.busy) {
                Text(localizedString(if (state.passwordVisible) R.string.auth_hide else R.string.auth_show))
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (lastField) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focus.moveFocus(FocusDirection.Down) },
            onDone = { focus.clearFocus(); onSubmit() },
        ),
        shape = MaterialTheme.shapes.small,
    )
}
