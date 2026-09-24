# Data Layer Guide

## Current implementation

Source paths below are relative to `app/src/main/java/studio/appvero/bikecare`.

| Component | Location | Behavior |
| --- | --- | --- |
| `AuthRepository` | `features/auth/data/repository` | Reads `FirebaseAuth.currentUser != null` |
| `AppPreferences` | `core/datastore` | Defines suspend language read/write operations |
| `AppPreferencesImpl` | `core/datastore` | Implements preferences with DataStore |
| `LanguageManager` | `core/localization` | Exposes language StateFlow and coordinates persistence |

`AuthRepository` is a concrete constructor-injected class. It has no repository
interface or separate remote data source. Its session check is synchronous and
does not perform a network refresh or read a locally stored user id.

`AppPreferencesImpl.getLanguage()` reads the first DataStore emission and maps
the tag through `AppLanguage.fromTag`. Missing and unknown tags resolve to English.
`saveLanguage()` writes the language tag with DataStore `edit`.
The preference file name is `bikecare_preferences`.

`PreferenceKeys.USER_ID` is declared in `AppPreferenceKeys.kt` but is unused.
Only language operations are exposed by `AppPreferences`.

## Ownership and injection

Repositories own feature data access, transformations, and future synchronization.
They must not own navigation, composables, snackbars, or other presentation work.
ViewModels convert results into UiState and SideEffect.

Shared preferences and localization live in core rather than an auth-specific
repository. Splash directly injects both `AuthRepository` and `LanguageManager`.

- `FirebaseModule` provides singleton `FirebaseAuth`.
- `DataStoreProviderModule` provides singleton `DataStore<Preferences>`.
- `DataStoreModule` binds singleton `AppPreferencesImpl` to `AppPreferences`.
- `LanguageManager` is a constructor-injected singleton.
- `AuthRepository` is constructor-injected without an explicit scope.

Use interfaces and separate data sources when they provide a concrete boundary
or support testing; the current repository does not require empty wrapper layers.
Do not introduce UseCase or Interactor classes.

## Offline-first target for business data

Bike/service/reminder storage and synchronization have not been implemented.
DataStore language preferences and a Firebase session snapshot do not constitute
the business-data offline architecture.

Future business-data repositories should expose local data as the first source
and follow this write flow:

```text
UI -> ViewModel -> Repository -> Local storage -> Sync queue -> Firebase
```

Persist user changes and pending synchronization locally before remote upload.
Define retry, conflict, and sign-out behavior when implementing the queue.
Scope local records, queued writes, and remote paths by uid to avoid mixing
accounts. Do not use a saved preference uid as an authentication authority.

No local database, sync worker, Retrofit client, or Firestore data source exists
yet. Dependency additions require approval. See
[FIREBASE_GUIDE.md](FIREBASE_GUIDE.md) for remote ownership requirements.
