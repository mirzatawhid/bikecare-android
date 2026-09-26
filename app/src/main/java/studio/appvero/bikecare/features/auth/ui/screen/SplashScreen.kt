package studio.appvero.bikecare.features.auth.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.appvero.bikecare.R
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import studio.appvero.bikecare.ui.theme.AppTheme
import studio.appvero.bikecare.ui.theme.BikeCareTheme


@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onEvent: (SplashEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = AppDimensions.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "BikeCare Logo",
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Text(
            text = "BikeCare",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Text(
            text = "Your bike, cared for.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxl))

        when (uiState) {
            SplashUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppSpacing.xl),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is SplashUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    Button(
                        modifier = Modifier.heightIn(min = AppDimensions.minimumTouchTarget),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.action,
                            contentColor = AppTheme.colors.onAction,
                        ),
                        onClick = {
                            onEvent(SplashEvent.Retry)
                        }
                    ) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

@Preview(name = "Splash Loading Light", widthDp = 412, showBackground = true)
@Preview(name = "Splash Loading Dark", widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashScreenLoadingPreview() {
    BikeCareTheme { SplashScreen(uiState = SplashUiState.Loading, onEvent = {}) }
}

@Preview(name = "Splash Error Light", widthDp = 412, showBackground = true)
@Preview(name = "Splash Error Dark", widthDp = 412, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashScreenErrorPreview() {
    BikeCareTheme {
        SplashScreen(
            uiState = SplashUiState.Error(message = "Unable to check authentication."),
            onEvent = {})
    }
}
