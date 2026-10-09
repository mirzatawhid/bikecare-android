package studio.appvero.bikecare.features.fuel.ui.screen

import androidx.compose.runtime.Composable
import studio.appvero.bikecare.core.localization.LocalLocale
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun fuelDate(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(LocalLocale.current)
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate())
