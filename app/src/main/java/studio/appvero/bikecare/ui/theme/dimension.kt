package studio.appvero.bikecare.ui.theme

import androidx.compose.ui.unit.dp

object AppSpacing {
    val none = 0.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 48.dp
}

object AppDimensions {
    // Reference for previews, never a fixed device width.
    val referenceCanvasWidth = 412.dp
    val screenHorizontalPadding = AppSpacing.lg
    val cardPadding = AppSpacing.lg
    val listItemVerticalPadding = AppSpacing.sm
    val minimumTouchTarget = 48.dp
    val buttonHeight = minimumTouchTarget
    val compactButtonHeight = minimumTouchTarget
    val iconButtonSize = minimumTouchTarget
    val dividerThickness = 1.dp
}

object AppMotion {
    // Use with Compose animation APIs (e.g. tween), which honor the platform
    // animator duration scale, including disabled animations. Do not use delays
    // to drive visual animation or override LocalMotionDurationScale.
    const val durationMillis = 180
}
