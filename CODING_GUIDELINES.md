# Kotlin and Compose Guidelines

## Names and packages

Use PascalCase for types and composable functions, camelCase for ordinary
functions and properties, and immutable `val` properties where possible.
Follow `features/<feature>/ui/screen`, `ui/viewmodel`, and `data/repository`.
Keep screen contracts together in `<Screen>Contracts.kt`.

The existing public flow names are `uiState` and singular `sideEffect`.
Private mutable backing flows use `_uiState` and `_sideEffect` and are exposed
with `asStateFlow()` and `asSharedFlow()`.

## Compose boundary

Screens receive state and event callbacks. The existing splash signature is:

```kotlin
@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onEvent: (SplashEvent) -> Unit
) {
    // Render uiState and emit user events through onEvent.
}
```

Route composables are the integration boundary: they may accept a default
`viewModel: SplashViewModel = hiltViewModel()`. They collect state with
`collectAsStateWithLifecycle()`, collect side effects in `LaunchedEffect`, and
pass values/callbacks to the screen. Keep ViewModels, repositories, SDKs, and
navigation controllers out of screen and reusable component parameters.

## ViewModels and coroutines

- Use Hilt constructor injection and `viewModelScope`; avoid `GlobalScope`.
- Define state transitions in response to events. Keep state immutable.
- Represent navigation and similar actions as side effects handled by the route.
- Use suspend functions for asynchronous operations; a synchronous SDK snapshot
  such as `AuthRepository.isUserLoggedIn()` does not need an artificial suspend API.
- Map expected failures into meaningful UI state. Preserve coroutine cancellation
  when handling exceptions around suspending work.
- Decide how repeated events behave: ignore, cancel, or serialize work as needed.
- Choose effect delivery deliberately. Splash's zero-replay SharedFlow does not
  guarantee delivery after a collector disconnects; a delay is not synchronization.

Prefer constructor injection. Avoid `lateinit` except where framework field
injection requires it, and ensure the host has the correct Hilt entry point.

## UI resources and scope

Use `BikeCareTheme`, `MaterialTheme`, shared spacing/dimension tokens, and
`localizedString` for UI resource strings. Splash's current literal English
strings are a documented gap, not a convention for new screens.

Keep composables focused and extract components when they have a clear reusable
responsibility. Do not add abstractions, packages, or dependencies speculatively.
See [UI_GUIDELINES.md](UI_GUIDELINES.md) for presentation details.
