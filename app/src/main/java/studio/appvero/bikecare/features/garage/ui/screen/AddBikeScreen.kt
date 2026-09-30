package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.theme.*

@Composable
fun AddBikeScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        Text(localizedString(R.string.garage_add_bike), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(R.string.garage_add_placeholder))
        OutlinedButton(onClick = onBack, modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget)) {
            Text(localizedString(R.string.garage_back))
        }
    }
}
