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
| `MaterialTheme.typography` | Space Grotesk and Manrope text styles |
| `MaterialTheme.shapes` | Shared shape styles |
| `AppTheme.colors` | Additional success, warning, error, and info colors |
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
