package studio.appvero.bikecare.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object BikeCareBrand {
    val Canvas = Color(0xFFF4F3EF)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF232622)
    val Muted = Color(0xFF6D716A)
    val Accent = Color(0xFFED6338)
    val AccentText = Color(0xFFB63B17)
    val Success = Color(0xFF26714D)
    val Warning = Color(0xFF8A5B0D)
}

// Primary is the readable text/outlined-control orange. Filled actions use
// AppTheme.colors.action / onAction so their labels remain readable.
val BikeCareLightColorScheme = lightColorScheme(
    primary = BikeCareBrand.AccentText,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCE),
    onPrimaryContainer = Color(0xFF561B08),
    secondary = BikeCareBrand.Muted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E6DF),
    onSecondaryContainer = BikeCareBrand.Ink,
    tertiary = BikeCareBrand.Muted,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5E6DF),
    onTertiaryContainer = BikeCareBrand.Ink,
    background = BikeCareBrand.Canvas,
    onBackground = BikeCareBrand.Ink,
    surface = BikeCareBrand.Surface,
    onSurface = BikeCareBrand.Ink,
    surfaceVariant = Color(0xFFE9E8E2),
    // Slightly darker than the foundation muted token for 4.5:1 on canvas.
    onSurfaceVariant = Color(0xFF6C7069),
    surfaceTint = BikeCareBrand.AccentText,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFDDDDD6),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9F8F5),
    surfaceContainer = BikeCareBrand.Canvas,
    surfaceContainerHigh = Color(0xFFEEEDE7),
    surfaceContainerHighest = Color(0xFFE9E8E2),
    outline = Color(0xFF7A7E76),
    outlineVariant = Color(0xFFCCCFC5),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = BikeCareBrand.Ink,
    inverseOnSurface = BikeCareBrand.Canvas,
    inversePrimary = Color(0xFFFFAD91),
    scrim = Color.Black,
)

val BikeCareDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFAD91),
    onPrimary = Color(0xFF561B08),
    primaryContainer = Color(0xFF713018),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = Color(0xFFC2C6BA),
    onSecondary = BikeCareBrand.Ink,
    secondaryContainer = Color(0xFF3D4139),
    onSecondaryContainer = Color(0xFFE5E6DF),
    tertiary = Color(0xFFC2C6BA),
    onTertiary = BikeCareBrand.Ink,
    tertiaryContainer = Color(0xFF3D4139),
    onTertiaryContainer = Color(0xFFE5E6DF),
    background = Color(0xFF171916),
    onBackground = BikeCareBrand.Canvas,
    surface = BikeCareBrand.Ink,
    onSurface = BikeCareBrand.Canvas,
    surfaceVariant = Color(0xFF353831),
    onSurfaceVariant = Color(0xFFB6BAB0),
    surfaceTint = Color(0xFFFFAD91),
    surfaceBright = Color(0xFF3C3F38),
    surfaceDim = Color(0xFF171916),
    surfaceContainerLowest = Color(0xFF11130F),
    surfaceContainerLow = Color(0xFF1D201B),
    surfaceContainer = BikeCareBrand.Ink,
    surfaceContainerHigh = Color(0xFF2C2F28),
    surfaceContainerHighest = Color(0xFF353831),
    outline = Color(0xFF90958A),
    outlineVariant = Color(0xFF484C43),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = BikeCareBrand.Canvas,
    inverseOnSurface = BikeCareBrand.Ink,
    inversePrimary = BikeCareBrand.AccentText,
    scrim = Color.Black,
)

@Immutable
data class AppColors(
    val action: Color,
    val onAction: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
)

val BikeCareLightAppColors = AppColors(
    action = BikeCareBrand.Accent,
    onAction = BikeCareBrand.Ink,
    success = BikeCareBrand.Success,
    warning = BikeCareBrand.Warning,
    error = BikeCareLightColorScheme.error,
    info = Color(0xFF345E80),
)
val BikeCareDarkAppColors = AppColors(
    action = BikeCareBrand.Accent,
    onAction = BikeCareBrand.Ink,
    success = Color(0xFF88D5AA),
    warning = Color(0xFFEFC477),
    error = BikeCareDarkColorScheme.error,
    info = Color(0xFFA2CBEA),
)

val LocalAppColors = staticCompositionLocalOf { BikeCareLightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}
