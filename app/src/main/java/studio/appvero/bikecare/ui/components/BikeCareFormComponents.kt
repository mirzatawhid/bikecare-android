package studio.appvero.bikecare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import studio.appvero.bikecare.ui.theme.AppTheme
import studio.appvero.bikecare.ui.theme.BikeCarePillShape

/** Shared primary action used for the app's prominent orange actions. */
@Composable
fun BikeCarePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentDescription: String = text,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppDimensions.minimumTouchTarget)
            .semantics { this.contentDescription = contentDescription },
        shape = BikeCarePillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.action,
            contentColor = AppTheme.colors.onAction,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.heightIn(max = AppSpacing.md),
                strokeWidth = AppSpacing.xxs,
                color = AppTheme.colors.onAction,
            )
        } else {
            leadingContent?.invoke(this)
            Text(text)
        }
    }
}

/** Shared icon-only control with the project's minimum accessible touch target. */
@Composable
fun BikeCareIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = AppDimensions.iconButtonSize)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        content = content,
    )
}

/** Shared labelled single-line form field with consistent validation treatment. */
@Composable
fun BikeCareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    required: Boolean = false,
    error: String? = null,
    enabled: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            placeholder = placeholder.takeIf { it.isNotBlank() }?.let { { Text(it) } },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = MaterialTheme.shapes.small,
        )
    }
}
