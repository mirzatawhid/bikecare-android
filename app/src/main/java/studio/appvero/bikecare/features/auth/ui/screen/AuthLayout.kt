package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.*

@Composable
internal fun AuthLayout(
    title: String,
    subtitle: String,
    state: AuthFormState,
    submitLabel: String,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    footer: @Composable () -> Unit,
    fields: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = 520.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimensions.screenHorizontalPadding, vertical = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(56.dp))
                Column {
                    Text(localizedString(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Text(localizedString(R.string.auth_tagline), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    fields()
                    state.error?.let { AuthNotice(localizedString(it), isError = true) }
                    state.message?.let { AuthNotice(localizedString(it), isError = false) }
                    Button(
                        onClick = onSubmit,
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget),
                        shape = BikeCarePillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.action, contentColor = AppTheme.colors.onAction),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(AppSpacing.xs))
                            Text(localizedString(R.string.auth_please_wait))
                        } else Text(submitLabel)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        HorizontalDivider(Modifier.weight(1f))
                        Text(localizedString(R.string.auth_or), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(Modifier.weight(1f))
                    }
                    OutlinedButton(
                        onClick = onGoogle,
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget),
                        shape = BikeCarePillShape,
                    ) {
                        if (state.isGoogleLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(AppSpacing.xs))
                        }
                        Text(localizedString(R.string.auth_google))
                    }
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { footer() }
        }
    }
}

@Composable
private fun AuthNotice(message: String, isError: Boolean) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(message, modifier = Modifier.padding(AppSpacing.sm),
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium)
    }
}
