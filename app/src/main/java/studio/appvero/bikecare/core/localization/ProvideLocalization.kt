package studio.appvero.bikecare.core.localization

import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale
import androidx.compose.ui.platform.LocalResources

@Composable
fun ProvideLocalization(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val currentConfiguration = LocalConfiguration.current

    val locale = remember(language.tag) {
        Locale.forLanguageTag(language.tag)
    }

    val localizedContext = remember(baseContext, currentConfiguration, locale) {
        val configuration = Configuration(currentConfiguration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
        }
        baseContext.createConfigurationContext(configuration)
    }

    val layoutDirection = remember(locale) {
        if (TextUtils.getLayoutDirectionFromLocale(locale) == android.view.View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    CompositionLocalProvider(
        LocalLocalizedContext provides localizedContext,
        LocalLocalizedResources provides localizedContext.resources,
        LocalLocale provides locale,
        LocalLayoutDirection provides layoutDirection,
    ) {
        content()
    }
}

@Composable
fun localizedString(
    @StringRes id: Int,
    vararg args: Any,
): String {
    val localizedResources = LocalLocalizedResources.current ?: LocalResources.current

    return if (args.isEmpty()) {
        localizedResources.getString(id)
    } else {
        localizedResources.getString(id, *args)
    }
}