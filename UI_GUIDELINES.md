# Compose UI Guidelines

## Route and screen separation

The splash feature establishes two UI responsibilities:

- `SplashRoute` obtains the Hilt ViewModel, collects state/effects, and invokes
  navigation callbacks.
- `SplashScreen(uiState, onEvent)` renders content and emits user events.

Keep screens and reusable components stateless. Do not pass them ViewModels,
Firebase instances, repositories, or NavControllers. Route composables are the
allowed ViewModel integration boundary.

Define each screen's contracts in its `<Screen>Contracts.kt`. Model only useful
states: splash renders Loading or Error and emits navigation effects on success.
It does not need Empty or Success UI variants.

## Existing splash presentation

Splash renders the logo, app name, tagline, and either a progress indicator or an
error message with a Retry button. Retry emits `SplashEvent.Retry`.
The file includes Loading and Error previews wrapped in `BikeCareTheme`.

Maintain previewable screens that require only state and callbacks. Add preview
states when new UI branches are introduced.

## Theme and layout

The existing shared design system lives under
`app/src/main/java/studio/appvero/bikecare/ui/theme`:

| API | Purpose |
| --- | --- |
| `BikeCareTheme` | Material 3 theme; light/dark schemes and optional dynamic color |
| `MaterialTheme.colorScheme` | Standard semantic colors |
| `MaterialTheme.typography` | Android sans-serif with Bengali system fallback |
| `MaterialTheme.shapes` | Shared shape styles |
| `AppTheme.colors` | Action/on-action and success, warning, error, info colors |
| `AppSpacing` / `AppDimensions` | Spacing and semantic dimensions |

Prefer these tokens over raw colors or repeated dimensions. Splash uses
`AppSpacing`, Material typography/colors, and some literal dimensions.
Use Material 3 Surface, Scaffold, and Cards where their behavior fits the screen;
splash currently uses a centered Column inside the activity's Surface.

Keep feature components with their feature. Create `core/ui/components` for
components shared across features when needed; that package does not exist yet.

## Localization and accessibility

`LanguageManager` exposes English/Bangla selection. `BikeCareApp` observes it
and wraps the navigation host in `ProvideLocalization`. Use resource strings
through `localizedString(R.string.some_label)` to consume the provided localized
resources. `ProvideLocalization` also supplies layout direction.

Splash currently hardcodes its app label, tagline, logo description, Retry text,
and error fallback in English. Resource migration is still needed. The app also
has duplicate nested localization providers; see
[ARCHITECTURE.md](ARCHITECTURE.md).

Provide meaningful accessibility descriptions for informative images and controls.
Check readable error messages, text scaling, touch targets, and light/dark
appearance when changing UI. Keep business and authentication decisions outside
composable rendering.

## BikeCare visual system

Use warm canvas `#F4F3EF`, white surfaces, ink `#232622`, and muted text
`#6D716A`. `BikeCareTheme` follows system dark mode by default; dark mode uses
charcoal canvas/surfaces and lighter text/status colors. Dynamic color is opt-in
and should remain disabled for the brand palette.

`MaterialTheme.colorScheme.primary` is readable action text (`#B63B17` in light
mode, light peach in dark mode). For orange filled actions, pass
`AppTheme.colors.action` and `onAction` to Material button/FAB colors. The orange
fill is `#ED6338` with ink labels; white small text on this orange lacks contrast.
Success (`#26714D`) and warning (`#8A5B0D`) have lighter dark-mode counterparts.
Always accompany a status color with a text label.

Typography maps heading to 34/41 medium, section to 22/33 medium, body to 16/24
regular, label to 14/21 semibold, and metadata to 12/18 regular (sp). Android
sans-serif provides Roboto on standard Android and system fallback for Bengali.
Nirmala UI is not bundled; using that exact font requires a licensed font asset.

Spacing uses 4, 8, 12, 16, 24, 32, and 48 dp. Shapes use 14, 24, and 32 dp;
`BikeCarePillShape` provides fully rounded controls. Keep touch targets at least
48 dp and allow controls to grow with text. Use the 412 dp canvas for previews,
not a fixed runtime width. Future destination screens should use a four-item
Material navigation bar, extended action button, and modal bottom sheets.
Those components are not currently implemented by the placeholder destinations.

Use `AppMotion.durationMillis` (180 ms) for custom Compose tween animations.
Compose animations must retain the platform motion duration scale, including
zero for disabled animation; do not drive visual animation with coroutine delays.
This token does not override Material components' internal motion specs.

Small muted text uses #6C7069 to meet 4.5:1 against the warm canvas; the original
#6D716A foundation token remains available for use on white surfaces.
