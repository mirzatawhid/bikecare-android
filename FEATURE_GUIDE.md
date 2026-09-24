# Feature Development Guide

## Use splash as the structural reference

Place features under `app/src/main/java/studio/appvero/bikecare/features`.
Splash belongs to `auth`; do not create a separate splash feature just because
it is a separate screen.

```text
features/<feature>/
  ui/screen/
    <Screen>Contracts.kt
    <Screen>Route.kt
    <Screen>Screen.kt
  ui/viewmodel/
    <Screen>ViewModel.kt
  data/repository/
    <Feature>Repository.kt
```

Shared infrastructure stays under `core/`, Hilt modules under `di/`, and app
navigation under `navigation/`. Add data sources and model packages only as
needed. Do not introduce UseCase or Interactor layers.

## Implementation sequence

1. Inspect splash and the existing shared infrastructure before adding code or
   dependencies. New dependencies require approval.
2. Define UiState, Event, and SideEffect in the screen's contracts file. Use
   sealed interfaces for alternatives and immutable data classes for payloads.
3. Implement repository operations and any needed local/remote data sources.
   Keep SDK access and persistence out of the UI.
4. Add a constructor-injected `@HiltViewModel`, private mutable flows, read-only
   `uiState` and `sideEffect`, and `onEvent(event)`. Use `viewModelScope`.
5. Implement a stateless screen accepting `uiState` and `onEvent`. Add previews
   for its meaningful states without a ViewModel or Firebase connection.
6. Implement a route composable that resolves the ViewModel, collects state
   with `collectAsStateWithLifecycle()`, and handles effects in `LaunchedEffect`.
   Express navigation as callbacks.
7. Declare serializable destinations in `navigation/AppRoute.kt` and register
   their content and callbacks in `navigation/AppNavHost.kt`.
8. Verify relevant state transitions, interactions, and navigation behavior.

## Contracts should match the screen

Splash illustrates the distinction between persistent UI state and actions:

| Contract | Current values |
| --- | --- |
| `SplashUiState` | `Loading`, `Error(message)` |
| `SplashEvent` | `CheckAuthentication`, `Retry` |
| `SplashSideEffect` | `NavigateToHome`, `NavigateToLogin` |

Do not force every screen to have Loading, Empty, Success, and Error variants.
A data list may need all four; splash navigates on success. Startup checks are
triggered in the ViewModel initializer, so the route should not also dispatch
the same startup event.

## Completion checklist

- Screen rendering depends on UiState; interactions emit Events.
- Route owns ViewModel integration; the app graph owns navigation execution.
- Repository owns feature data access; shared services reuse existing core APIs.
- New business-data features implement local-first storage and uid ownership.
- UI uses the shared theme, resource strings, and relevant previews.
- Coroutine failure, cancellation, repeated events, and effect delivery have
  explicit behavior and appropriate verification.
- Dependencies, DI wiring, route serializers, and destination content are wired.

The existing splash has unfinished startup and effect-handling details. Consult
[ARCHITECTURE.md](ARCHITECTURE.md) before copying those behaviors.
