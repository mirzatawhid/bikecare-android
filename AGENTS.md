# BikeCare development

Inspect only files relevant to the task. Treat source as truth. Read `PROJECT_NOTES.md` only for startup/authentication or build-specific details. Do not scan unrelated files or docs.

## Stack

Single Android `app` module using Kotlin, Compose Material 3, MVVM, Hilt, typed Navigation Compose, Firebase Authentication/Firestore/Storage/Analytics, and DataStore for English/Bangla preferences. Room is the planned local cache for future business persistence; Crashlytics is deferred until configured.

Email/password auth, required email verification, logout, and hosted password recovery exist. Home is temporary. Bike/service/reminder persistence and sync are not implemented. Google sign-in is deferred.

Do not add dependencies unless requested.

## Structure

Source:
`app/src/main/java/studio/appvero/bikecare`

```text
features/<feature>/ui/screen/       <Screen>Contracts.kt, <Screen>Route.kt, <Screen>Screen.kt
features/<feature>/ui/viewmodel/    <Screen>ViewModel.kt
features/<feature>/data/repository/ repositories
core/                               shared preferences/localization
di/                                 Hilt modules
navigation/                         AppRoute.kt, AppNavHost.kt
ui/theme/                           design tokens
```

Splash belongs to `features/auth`.

Use a `domain` package only for models when needed. Do not introduce UseCase/Interactor layers or parallel `feature/` / `presentation/` architectures.

## MVVM

- Keep `UiState`, `Event`, and `SideEffect` in `<Screen>Contracts.kt`.
- Screens are stateless: receive state/events and contain no ViewModel, repository, SDK, or NavController.
- `@HiltViewModel`s expose `StateFlow` state, `SharedFlow` effects, and `onEvent`.
- Use immutable state and `viewModelScope`; preserve coroutine cancellation.
- Routes use `hiltViewModel()`, `collectAsStateWithLifecycle()`, collect effects, and invoke navigation callbacks.
- Typed `@Serializable` destinations live in `AppRoute.kt`; navigation execution belongs in `AppNavHost.kt`.
- Repositories own data access, not UI or navigation.
- Reuse `LanguageManager -> AppPreferences -> DataStore`.

## Data and auth

Firebase Authentication is the source of user identity and session persistence. Use the authenticated Firebase UID, never a saved user ID or boolean preference, as identity.

Firestore is the primary cloud database. All user data must be scoped under the authenticated UID. Firestore security rules are mandatory for every user-owned resource; require ownership and verified email. Storage objects also require UID ownership rules.

Keep repository interfaces and implementations in the existing data/repository folders. ViewModels depend directly on repository interfaces so future backend migration remains possible. Never access Firebase from Compose UI or ViewModels. All SDK operations and exception mapping belong in repository implementations; Firebase models must not leak into domain models.

Prefer offline-first business persistence using Room where needed:
Repository -> Room -> Sync queue -> Firestore.
Do not create unused caches or sync infrastructure. Define retries, conflicts, and account-switch isolation when implementing business persistence.

Never store passwords or auth tokens in DataStore/SavedStateHandle. Firebase owns authentication persistence. Password reset uses a hosted email link and never signs in the app. Require verification before Home, refresh user/token after verification, and clear protected navigation on logout/session loss.

## UI

Use `BikeCareTheme`, Material tokens, `AppSpacing`, `AppDimensions`, and `localizedString`.

Keep system dark mode, brand palette, and dynamic color disabled.

Use `AppTheme.colors.action/onAction` for orange actions. Maintain accessible contrast, text with status colors, 48 dp touch targets, scalable UI, and accessibility labels.

Support English/Bangla and light/dark themes. `412 dp` is preview-only, never a runtime width constraint.

Use `AppMotion.durationMillis` for custom Compose tweens.

## Verification

Use the smallest relevant verification.

- Docs/comments only: no Gradle command.
- Kotlin/Compose changes: run `.\gradlew.bat :app:compileDebugKotlin -q`.
- Manifest, resources, dependency, or build configuration changes: run `.\gradlew.bat :app:assembleDebug -q`.
- Bug fixes or test changes: run only the directly relevant existing test when practical.
- Do not create tests unless requested or needed to reproduce a bug.
- Do not run the full unit suite, lint, Android tests, connected tests, or full verification suite unless explicitly requested.
- Do not rerun successful checks unless subsequent edits could invalidate them.

Report only verification actually performed and any unresolved failures.