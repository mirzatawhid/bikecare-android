package studio.appvero.bikecare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppTheme
import studio.appvero.bikecare.ui.theme.BikeCarePillShape

@Composable
fun BikeCareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    outlined: Boolean = false,
) {
    val buttonModifier = modifier.fillMaxWidth().heightIn(min = AppDimensions.minimumTouchTarget)
    val content: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.heightIn(min = 20.dp, max = 20.dp),
                    color = if (outlined) MaterialTheme.colorScheme.primary else AppTheme.colors.onAction,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text)
            }
        }
    }
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = BikeCarePillShape,
            content = { content() },
        )
    } else {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = BikeCarePillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.action,
                contentColor = AppTheme.colors.onAction,
            ),
            content = { content() },
        )
    }
}
