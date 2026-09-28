# Firebase Guide

## Authentication update

Email/password login, registration, password reset, and Google sign-in are now
implemented. Credential Manager supplies a Google token and AuthRepository
exchanges it with Firebase Auth. MainActivity now has `@AndroidEntryPoint`.
See [AUTH_SETUP.md](AUTH_SETUP.md) for the required provider/fingerprint/OAuth
configuration. The earlier baseline below describes the pre-auth implementation;
its missing authentication flows and activity annotation have been addressed.
Sign-out, Firestore, Storage, FCM, and business-data synchronization remain future work.

## Implemented integration

The app declares Firebase Auth and Analytics dependencies and the Firebase BOM.
The Google Services Gradle plugin is applied. Current feature code uses Auth
through `di/FirebaseModule.kt`, which provides a singleton `FirebaseAuth`.

`features/auth/data/repository/AuthRepository.kt` exposes:

```kotlin
fun isUserLoggedIn(): Boolean {
    return firebaseAuth.currentUser != null
}
```

Splash uses this snapshot to choose Home or Login. It does not sign a user in,
refresh a token, validate a session with the backend, or use the DataStore
`USER_ID` key. Keep Firebase calls inside repositories/data sources.

Email/password login, Google login, registration, and sign-out flows are not
implemented. FirebaseAuth's current user is the session source used by splash;
do not create a second authentication flag in preferences.

## Dependency injection follow-up

`BikeCareApplication` has `@HiltAndroidApp` and is registered in the manifest.
`MainActivity` currently lacks `@AndroidEntryPoint` despite injecting
`LanguageManager` and hosting Hilt ViewModels. Correct and verify that startup
wiring before treating the splash flow as operational.

## Future user-data integration

Firestore, Storage, FCM, their repositories, and security rules are not implemented
in the inspected project. These paths describe the intended ownership model:

```text
users/{uid}/bikes/{bikeId}
users/{uid}/services/{serviceId}
users/{uid}/reminders/{reminderId}
users/{uid}/receipts/
```

- Derive uid from the authenticated Firebase session.
- Scope reads, writes, local caches, and sync work to that uid.
- Validate authenticated ownership in Firestore and Storage security rules.
  Client-side path construction alone is not an access-control boundary.
- Store receipt references/URLs in Firestore when Storage support is implemented.
- Handle signed-out and account-switch states before reading or syncing user data.
- Verify rules against unauthorized and cross-user requests before deployment.

Follow the local-first write contract in
[DATA_LAYER_GUIDE.md](DATA_LAYER_GUIDE.md). Obtain approval before adding SDK
dependencies; do not describe the above integrations as already available.
