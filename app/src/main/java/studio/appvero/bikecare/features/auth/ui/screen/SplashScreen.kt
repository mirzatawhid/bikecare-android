package studio.appvero.bikecare.features.auth.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import studio.appvero.bikecare.ui.theme.AppSpacing
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onEvent: (SplashEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
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
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Text(
            text = "Your bike, cared for.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxl))

        when (uiState) {
            SplashUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppSpacing.xl)
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

@Preview(name = "Splash Loading", showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenLoadingPreview() {
    BikeCareTheme { SplashScreen(uiState = SplashUiState.Loading, onEvent = {}) }
}

@Preview(name = "Splash Error", showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenErrorPreview() {
    BikeCareTheme {
        SplashScreen(
            uiState = SplashUiState.Error(message = "Unable to check authentication."),
            onEvent = {})
    }
}