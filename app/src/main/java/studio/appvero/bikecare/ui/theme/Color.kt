package studio.appvero.bikecare.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/*
 * ============================================================
 * BRAND PRIMITIVES
 * ============================================================
 *
 * These are the only places where the brand's raw colors live.
 *
 * Do not use these directly from screens.
 * Screens should consume MaterialTheme.colorScheme or
 * AppTheme.colors.
 */

object BikeTrackerBrand {

    // Primary brand identity
    val DeepForestGreen = Color(0xFF0B1F17)
    val RichMotorcycleGreen = Color(0xFF123D2A)

    // Accent
    val ElectricLime = Color(0xFFB7F34A)

    // Light foundation
    val WarmOffWhite = Color(0xFFF5F7F3)

    // Neutral
    val NearBlack = Color(0xFF101412)
    val MutedGreenGray = Color(0xFF66716B)

    // Semantic colors
    val Success = Color(0xFF3FA66B)
    val Warning = Color(0xFFF2B84B)
    val Error = Color(0xFFD94A4A)
    val Info = Color(0xFF4C8DFF)

    // Dark-mode secondary text requested by the design spec.
    //
    // This is deliberately kept as a theme-level token rather
    // than being scattered through UI code.
    val DarkSecondaryText = Color(0xFFA8B2AC)

    // Surface used by the dark theme.
    val DarkBackground = Color(0xFF08120E)
}


/*
 * ============================================================
 * LIGHT MATERIAL 3 COLOR SCHEME
 * ============================================================
 */

val BikeTrackerLightColorScheme: ColorScheme = lightColorScheme(

    primary = BikeTrackerBrand.DeepForestGreen,
    onPrimary = BikeTrackerBrand.WarmOffWhite,

    primaryContainer = BikeTrackerBrand.RichMotorcycleGreen,
    onPrimaryContainer = BikeTrackerBrand.ElectricLime,

    secondary = BikeTrackerBrand.RichMotorcycleGreen,
    onSecondary = BikeTrackerBrand.WarmOffWhite,

    secondaryContainer = BikeTrackerBrand.MutedGreenGray,
    onSecondaryContainer = BikeTrackerBrand.WarmOffWhite,

    tertiary = BikeTrackerBrand.ElectricLime,
    onTertiary = BikeTrackerBrand.NearBlack,

    tertiaryContainer = BikeTrackerBrand.ElectricLime,
    onTertiaryContainer = BikeTrackerBrand.NearBlack,

    background = BikeTrackerBrand.WarmOffWhite,
    onBackground = BikeTrackerBrand.NearBlack,

    surface = Color.White,
    onSurface = BikeTrackerBrand.NearBlack,

    surfaceVariant = BikeTrackerBrand.MutedGreenGray,
    onSurfaceVariant = BikeTrackerBrand.WarmOffWhite,

    outline = BikeTrackerBrand.MutedGreenGray,
    outlineVariant = BikeTrackerBrand.MutedGreenGray.copy(alpha = 0.55f),

    error = BikeTrackerBrand.Error,
    onError = Color.White,

    errorContainer = BikeTrackerBrand.Error.copy(alpha = 0.14f),
    onErrorContainer = BikeTrackerBrand.Error,

    inverseSurface = BikeTrackerBrand.NearBlack,
    inverseOnSurface = BikeTrackerBrand.WarmOffWhite,
    inversePrimary = BikeTrackerBrand.ElectricLime,

    scrim = Color.Black
)


/*
 * ============================================================
 * DARK MATERIAL 3 COLOR SCHEME
 * ============================================================
 *
 * This is intentionally NOT a simple inversion of light mode.
 *
 * Visual direction:
 *
 * Carbon
 * + Motorcycle Green
 * + Electric Lime
 */

val BikeTrackerDarkColorScheme: ColorScheme = darkColorScheme(

    primary = BikeTrackerBrand.ElectricLime,
    onPrimary = BikeTrackerBrand.NearBlack,

    primaryContainer = BikeTrackerBrand.RichMotorcycleGreen,
    onPrimaryContainer = BikeTrackerBrand.ElectricLime,

    secondary = BikeTrackerBrand.RichMotorcycleGreen,
    onSecondary = BikeTrackerBrand.WarmOffWhite,

    secondaryContainer = BikeTrackerBrand.DeepForestGreen,
    onSecondaryContainer = BikeTrackerBrand.WarmOffWhite,

    tertiary = BikeTrackerBrand.ElectricLime,
    onTertiary = BikeTrackerBrand.NearBlack,

    tertiaryContainer = BikeTrackerBrand.RichMotorcycleGreen,
    onTertiaryContainer = BikeTrackerBrand.ElectricLime,

    background = BikeTrackerBrand.DarkBackground,
    onBackground = BikeTrackerBrand.WarmOffWhite,

    surface = BikeTrackerBrand.DeepForestGreen,
    onSurface = BikeTrackerBrand.WarmOffWhite,

    surfaceVariant = BikeTrackerBrand.RichMotorcycleGreen,
    onSurfaceVariant = BikeTrackerBrand.DarkSecondaryText,

    outline = BikeTrackerBrand.MutedGreenGray,
    outlineVariant = BikeTrackerBrand.MutedGreenGray.copy(alpha = 0.60f),

    error = BikeTrackerBrand.Error,
    onError = Color.White,

    errorContainer = BikeTrackerBrand.Error.copy(alpha = 0.18f),
    onErrorContainer = Color.White,

    inverseSurface = BikeTrackerBrand.WarmOffWhite,
    inverseOnSurface = BikeTrackerBrand.NearBlack,
    inversePrimary = BikeTrackerBrand.DeepForestGreen,

    scrim = Color.Black
)


/*
 * ============================================================
 * CUSTOM SEMANTIC COLORS
 * ============================================================
 *
 * Material 3 has error, but it does not have first-class
 * application semantics for success/warning/info.
 */

@Immutable
data class AppColors(
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color
)


val BikeTrackerLightAppColors = AppColors(
    success = BikeTrackerBrand.Success,
    warning = BikeTrackerBrand.Warning,
    error = BikeTrackerBrand.Error,
    info = BikeTrackerBrand.Info
)


val BikeTrackerDarkAppColors = AppColors(
    success = BikeTrackerBrand.Success,
    warning = BikeTrackerBrand.Warning,
    error = BikeTrackerBrand.Error,
    info = BikeTrackerBrand.Info
)


/*
 * ============================================================
 * COMPOSITION LOCAL
 * ============================================================
 */

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    BikeTrackerLightAppColors
}


/*
 * ============================================================
 * PUBLIC ACCESSOR
 * ============================================================
 *
 * Usage:
 *
 * AppTheme.colors.success
 * AppTheme.colors.warning
 * AppTheme.colors.error
 * AppTheme.colors.info
 */

object AppTheme {

    val colors: AppColors
        @androidx.compose.runtime.Composable
        get() = LocalAppColors.current
}