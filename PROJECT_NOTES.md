# BikeCare setup and known gaps

Read the relevant section when working on startup, authentication, or builds.
These notes reflect source inspection, not a fresh build or device validation.
Paths below are relative to `app/src/main/java/studio/appvero/bikecare`.

## Startup and authentication

`features/auth/data/repository/AuthRepository.kt` handles Firebase email login,
registration, password reset, and Google token exchange. Its session check reads
`FirebaseAuth.currentUser != null`; it does not refresh or validate with a server.
Firebase persists the session; do not add a preference-based authentication flag.
The declared DataStore `USER_ID` key is unused. Language preferences default to
English for missing/unknown tags.

`navigation/AppNavHost.kt` registers Splash, Login, Register, and Home. Home renders
`AuthSuccessScreen`; the other bike/maintenance/profile route declarations have
no destination content yet. Successful authentication clears auth forms from Back
history; splash navigation removes splash. Sign-out remains unimplemented.
`MainActivity` has `@AndroidEntryPoint`, serialization is enabled, and
`BikeCareApp` supplies one localization provider.

Known splash gaps in `features/auth/ui/viewmodel/SplashViewModel.kt`:

- Language loading and authentication run independently. Language failures are
  not mapped to splash errors; Retry only repeats authentication.
- Successful authentication checks wait three seconds before navigation.
  Zero-replay `SharedFlow(extraBufferCapacity = 1)` loses effects when no collector
  exists; the delay does not guarantee delivery. State stays Loading on success.
- Repeated CheckAuthentication/Retry events can launch overlapping jobs and
  duplicate navigation. Do not dispatch startup again from the route.
- `SplashScreen.kt` and the error fallback still contain English literals.

## Local build and Google sign-in setup

`app/build.gradle.kts` requires `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`
in `local.properties`, alongside the local Android SDK configuration.
`di/SupabaseModule.kt` provides Auth, Postgrest, and Storage; no feature repository
currently consumes them. This wiring does not establish business-data sync.

For the Firebase Android app `studio.appvero.bikecare`:

1. Enable Email/Password and Google providers in Firebase Authentication; configure
   Google's support email.
2. Register SHA-1/SHA-256 for the debug, release, and Play signing certificates used.
   Obtain local fingerprints with `.\gradlew.bat :app:signingReport`.
3. Replace `app/google-services.json` with the updated configuration. The currently
   checked-in file has no OAuth clients. Google Services must generate
   `default_web_client_id` from a Web OAuth client (type 3), not an Android client ID.
4. Rebuild and verify on a device/emulator with Google Play services and an account.

`features/auth/ui/screen/GoogleSignIn.kt` reports a localized setup error when the
client ID is missing; email authentication remains usable. Firebase validates
the Google token. Language splitting is disabled to retain English/Bangla for
the in-app switcher.

## Verification

From the repository root in PowerShell, run tasks relevant to the change:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:connectedDebugAndroidTest
```

The last task requires a device/emulator. Existing coverage is six
`AuthValidationTest` cases, three `AuthScreensTest` cases, and generated template
tests. Auth tests cover validation, form actions, busy controls, and password
masking; they do not verify Firebase connectivity. Splash, repository, localization,
and navigation behavior lack dedicated automated coverage. Coroutine-test, mocking,
and Hilt testing libraries are not declared; dependency additions require approval.

When changing startup, verify repeated events, collector detach/reattach, language
failure/retry, cancellation, and Back behavior. Collect effects before triggering
work; use controlled coroutine time when test support is available, not real sleeps.

For auth changes, check valid/invalid login, duplicate registration, weak passwords,
reset email delivery, session persistence after restart, Google success/cancellation,
missing configuration, offline errors, repeated taps, rotation/backgrounding, and
Back after success. Check keyboard scrolling, large text, both languages and themes.
For future sync, verify offline writes, retries/conflicts, account switching, and
uid isolation including backend rules. Report build, device, and live-service
verification separately.
