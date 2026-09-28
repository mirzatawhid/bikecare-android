package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.AppSpacing

/** Temporary Home content until the bike dashboard is implemented. */
@Composable
fun AuthSuccessScreen() {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(AppSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(localizedString(R.string.auth_success_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(AppSpacing.md))
        Text(localizedString(R.string.auth_success_body), textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
