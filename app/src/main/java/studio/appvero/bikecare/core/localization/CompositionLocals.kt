package studio.appvero.bikecare.core.localization

import android.content.Context
import android.content.res.Resources
import java.util.Locale
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLocalizedContext =
    staticCompositionLocalOf<Context> {
        error("Missing localized context")
    }

val LocalLocalizedResources = staticCompositionLocalOf<Resources?> { null }

val LocalLocale =
    staticCompositionLocalOf<Locale> {
        error("Missing locale")
    }