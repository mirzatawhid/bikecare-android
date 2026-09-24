# BikeCare - Codex Development Instructions

Act as a senior Android engineer working with Kotlin, Jetpack Compose, MVVM,
Firebase, and offline-first Android apps. Inspect the implementation before
making changes and follow splash as the current structural reference.

## Project and stack

BikeCare is intended to support motorcycle profiles, service logs, maintenance
reminders, notifications, offline data, and Firebase synchronization.

Currently implemented: a single `app` module, Compose + Material 3, Hilt wiring,
Firebase Auth session checking, DataStore language preferences, localization,
and typed Navigation Compose declarations. Splash is under `features/auth`.
Login and Home destinations are placeholders.

Firestore, Storage, FCM, Retrofit, a business-data local store, and a sync queue
are planned capabilities; they are not implemented dependencies or features yet.
Do not add dependencies without approval.

## Architecture rules

The source root is `app/src/main/java/studio/appvero/bikecare`.
Use the existing feature-first structure:

```text
features/<feature>/
  ui/screen/         # <Screen>Contracts.kt, <Screen>Route.kt, <Screen>Screen.kt
  ui/viewmodel/      # <Screen>ViewModel.kt
  data/repository/   # Repository implementations
core/                # Shared preferences, localization, constants
di/                  # Hilt modules
navigation/          # AppRoute.kt and AppNavHost.kt
ui/theme/            # Shared design tokens and theme
```

Add models or data-source packages only when needed. If a `domain` package is
introduced, keep it for models. Do not create UseCase or Interactor layers.
Do not introduce parallel `feature/` or `presentation/` conventions.

The normal interaction flow is:

```text
Screen -> Event -> ViewModel -> Repository -> Data source
ViewModel -> UiState -> Screen
ViewModel -> SideEffect -> Route -> Navigation callback -> AppNavHost
```

## UI and ViewModel rules

- Define `<Screen>UiState`, `<Screen>Event`, and `<Screen>SideEffect` together
  in `<Screen>Contracts.kt`. Model only states relevant to the screen.
- Keep mutable flows private. Expose `uiState` as `StateFlow` and `sideEffect`
  as `SharedFlow`, plus `onEvent(event)`, following splash naming.
- Use `@HiltViewModel`, constructor injection, and `viewModelScope`.
- Route composables may obtain a ViewModel with `hiltViewModel()`, collect state
  with `collectAsStateWithLifecycle()`, and collect effects in `LaunchedEffect`.
- Screen composables receive `uiState` and `onEvent`; keep them stateless and
  independent of ViewModels, Firebase, repositories, and navigation controllers.
- Keep business decisions in the ViewModel/repository and navigation execution
  in the app navigation graph.
- Treat splash as a structural reference, not a guarantee that startup is
  complete. Review the implementation gaps in [ARCHITECTURE.md](ARCHITECTURE.md).

## Data and Firebase rules

Repositories own data access; they must not navigate or render UI.
`AuthRepository` currently reads `FirebaseAuth.currentUser` directly.
Shared language preferences use `LanguageManager -> AppPreferences -> DataStore`.

For future bike/service/reminder data, local storage must be the first source:

```text
UI -> ViewModel -> Repository -> Local storage -> Sync queue -> Firebase
```

This is a target design, not an existing sync implementation. Scope all future
user data by authenticated Firebase uid, for example `users/{uid}/bikes/{bikeId}`.
Never allow cross-user access; a locally saved uid is not proof of authentication.

## Coding and verification

Prefer immutable models, Kotlin Flow, suspend functions, and clear names. Avoid
`GlobalScope`, unnecessary `lateinit`, and large composable files. Framework
field injection is an exception to the `lateinit` preference.

Use the existing theme and localization helpers. Handle meaningful error states
and preserve coroutine cancellation. Validate changes with checks appropriate to
their scope; do not claim runtime validation from a source review.

## Documentation map

- [Architecture and splash analysis](ARCHITECTURE.md)
- [Feature implementation checklist](FEATURE_GUIDE.md)
- [Kotlin and coroutine conventions](CODING_GUIDELINES.md)
- [Data ownership and offline-first target](DATA_LAYER_GUIDE.md)
- [Firebase integration and ownership rules](FIREBASE_GUIDE.md)
- [Typed routes and navigation effects](NAVIGATION_GUIDE.md)
- [Testing strategy and current coverage](TESTING_GUIDE.md)
- [Compose UI, theme, and localization](UI_GUIDELINES.md)
