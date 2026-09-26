package studio.appvero.bikecare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Android sans-serif (Roboto on standard Android), with system glyph fallback
// for Bengali. Nirmala UI is not bundled or assumed to be installed on Android.
private fun textStyle(size: Int, height: Int, weight: FontWeight) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
)

private val heading = textStyle(34, 41, FontWeight.Medium)
private val section = textStyle(22, 33, FontWeight.Medium)
private val body = textStyle(16, 24, FontWeight.Normal)
private val label = textStyle(14, 21, FontWeight.SemiBold)
private val metadata = textStyle(12, 18, FontWeight.Normal)

val BikeCareTypography = Typography(
    displayLarge = heading,
    displayMedium = heading,
    displaySmall = heading,
    headlineLarge = heading,
    headlineMedium = heading,
    headlineSmall = section,
    titleLarge = section,
    titleMedium = section,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = body,
    bodySmall = metadata,
    labelLarge = label,
    labelMedium = label,
    labelSmall = metadata,
)
