package studio.appvero.bikecare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import studio.appvero.bikecare.core.localization.LanguageManager
import studio.appvero.bikecare.core.localization.ProvideLocalization
import studio.appvero.bikecare.navigation.AppNavHost

@Composable
fun BikeCareApp(
    languageManager: LanguageManager,
) {

    val navController = rememberNavController()

    val language by languageManager.language.collectAsStateWithLifecycle()

    ProvideLocalization(language) {

        ProvideLocalization(language) {
            AppNavHost(
                navController = navController
            )
        }
    }
}