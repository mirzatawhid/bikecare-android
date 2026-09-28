# BikeCare development

Inspect relevant source before editing. Use splash as the structural reference,
not as a finished startup implementation. Read [PROJECT_NOTES.md](PROJECT_NOTES.md)
only for startup/authentication, build setup, or verification details. Keep rules
here and implementation detail in source; avoid duplicate guides.

## Current scope

Single Android `app` module: Kotlin, Compose Material 3, MVVM, Hilt, typed
Navigation Compose, Firebase Auth, and DataStore English/Bangla preferences.
Login, registration, password reset, and Google sign-in exist; Home is temporary
success content. Supabase dependencies and DI exist, but auth flows use Firebase.
Bike/service/reminder storage, offline sync, Firestore, Firebase Storage, FCM,
and Retrofit remain unimplemented. New dependencies require approval.

## Structure and flow

Source root: `app/src/main/java/studio/appvero/bikecare`.

```text
features/<feature>/ui/screen/       <Screen>Contracts.kt, <Screen>Route.kt, <Screen>Screen.kt
features/<feature>/ui/viewmodel/    <Screen>ViewModel.kt
features/<feature>/data/repository/ Repository implementations
core/                             Shared preferences and localization
di/                               Hilt modules
navigation/                       AppRoute.kt and AppNavHost.kt
ui/theme/                         Shared design tokens
```

Splash belongs to `features/auth`. Add model/data-source packages only as needed;
any `domain` package is for models. No UseCase/Interactor layers or parallel
`feature/` or `presentation/` conventions.

- Keep `<Screen>UiState`, `<Screen>Event`, and `<Screen>SideEffect` together in
  `<Screen>Contracts.kt`; model only meaningful states.
- Screens accept `uiState` and `onEvent`, render stateless UI, and emit events.
  No ViewModels, repositories, SDKs, or navigation controllers in screens.
- Constructor-injected `@HiltViewModel`s expose `uiState: StateFlow`,
  `sideEffect: SharedFlow`, and `onEvent(event)` with private mutable backing flows.
  Use immutable state and `viewModelScope`; preserve cancellation and define
  failure, repeated-event, and effect-delivery behavior. Avoid `GlobalScope` and
  unnecessary `lateinit` (framework field injection is allowed).
- Routes obtain `hiltViewModel()`, use `collectAsStateWithLifecycle()`, collect
  effects in `LaunchedEffect`, and invoke navigation callbacks. Business decisions
  belong in ViewModels/repositories. Register typed `@Serializable` destinations
  in `AppRoute.kt`; execute navigation and back-stack changes in `AppNavHost.kt`.
- Repositories own data access; no navigation or UI. Reuse
  `LanguageManager -> AppPreferences -> DataStore` for language preferences.

## Data ownership

Future business data must read locally first and persist writes before upload:
`Repository -> Local storage -> Sync queue -> Firebase`. Define retries, conflicts,
sign-out, and account switching. Scope local records, queued work, and remote
paths by authenticated Firebase uid (e.g. `users/{uid}/bikes/{bikeId}`); enforce
ownership with backend security rules and test cross-user denial. A saved uid is
not authentication. Keep passwords and tokens out of DataStore/SavedStateHandle.
Credential Manager account selection belongs at the route boundary; Firebase
token exchange belongs in the repository.

## UI and verification

Use `BikeCareTheme`, Material theme tokens, `AppSpacing`, `AppDimensions`, and
`localizedString` with English/Bangla resources. Keep system dark mode and the
brand palette (dynamic color off). Orange filled actions use `AppTheme.colors.action`
and `onAction`; small white labels lack contrast. Use text alongside status colors,
48 dp minimum touch targets, scalable controls, and meaningful accessibility labels.
412 dp is a preview width, not a runtime constraint. Use `AppMotion.durationMillis`
for custom Compose tweens, preserving platform motion scaling; no animation delays.
For future main destinations, use a four-item Material navigation bar, extended
action button, and modal bottom sheets as appropriate.

Add relevant stateless previews; check English/Bangla, large text, and light/dark
UI. Verify affected behavior and report what actually ran; source review is not
runtime validation. For code changes, use relevant Gradle tasks from
`PROJECT_NOTES.md`; for docs, check source accuracy, links, and diff scope.
