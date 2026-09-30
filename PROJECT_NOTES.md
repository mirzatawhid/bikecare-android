# BikeCare project notes

Read only when working on startup, authentication, Firebase configuration, or build setup.

Paths are relative to:
`app/src/main/java/studio/appvero/bikecare`

## Authentication

The flow is Screen -> ViewModel -> AuthRepository interface -> FirebaseAuthRepository. Keep the existing feature folders; no use-case or presentation layer is needed. AuthUser contains only UID, email, and verification status. Repository errors are SDK-independent and mapped to English/Bangla strings by the UI layer.

Firebase Authentication supports Email/Password initially and owns session persistence and refresh. Startup waits for the first auth-state callback before selecting Login, Verify Email, or Home. A DataStore user ID is not authentication.

Registration creates the account once and navigates to Verify Email, which sends the initial email independently. Delivery failure leaves the account available for resend. Unverified login and restored unverified sessions also enter Verify Email. Refresh reloads the user and refreshes the ID token before Home; foreground return triggers refresh. Offline failures remain retryable.

ForgotPassword sends a reset link with neutral account-existence messaging. Users choose a password on Firebase's hosted page and then sign in normally. No recovery code or Android app link is required. Passwords and auth tokens must never be stored in saved state.

Splash initializes language and auth before navigation, supports retry, preserves coroutine cancellation, and retains navigation effects until acknowledged.

Auth routes wait until resumed before navigation. Ignore repeated submissions while busy. Auth-boundary navigation clears the back stack. Temporary Home provides logout and observes session loss; foreground refresh detects invalidated accounts when online.

Google sign-in is deferred.

## Firebase setup

Backend: Firebase. Authentication: Firebase Authentication. Database: Cloud Firestore. Storage: Firebase Storage. Telemetry: Firebase Analytics. Crashlytics is not configured and remains deferred.

1. Use the existing Firebase project's Android app for package studio.appvero.bikecare and place its downloaded configuration at app/google-services.json.
2. Enable Authentication > Email/Password. Keep Google sign-in deferred.
3. Enable email enumeration protection and configure a password policy. The app validates at least six characters for signup; Firebase enforces the configured policy.
4. Configure verification and password-reset email templates, sender/support details, and the authorized Firebase-hosted action domain. Test delivery, expired links, and reused links using a dedicated test account. App UI supports English/Bangla; email-template language is configured separately in Firebase.
5. Create Cloud Firestore in production mode in the chosen region. Publish the root firestore.rules file in the Console Rules editor before allowing user data.
6. Enable/link Google Analytics if desired. The SDK is included; custom telemetry and per-bike aggregates are not implemented. Do not log credentials, tokens, or email addresses.
7. Storage SDK is included, but uploads and bucket activation are deferred. Cloud Storage requires Blaze billing even when usage falls within no-cost allowances. Set budget alerts before enabling billing; alerts do not cap spending.
8. Configure Crashlytics separately when crash reporting is introduced.

local.properties only needs Android SDK configuration (sdk.dir); backend credentials are no longer read from it. Never put service-account credentials in the app.

di/FirebaseModule.kt supplies FirebaseAuth and binds AuthRepository to FirebaseAuthRepository. Firebase default initialization uses the Google Services configuration.

Inspect Gradle/version files for current dependency versions rather than relying on this document.

If Java is unavailable from PATH, use Android Studio's bundled JBR.

English/Bangla language splitting is disabled.

## Future persistence

No bike/service/reminder business persistence currently exists.

Future offline-first business persistence uses Room where required:
Repository -> Room -> Sync queue -> Firestore.

Firestore document direction:
- users/{uid}
- users/{uid}/bikes/{bikeId}
- users/{uid}/fuelLogs/{fuelId}
- users/{uid}/maintenanceLogs/{maintenanceId}
- users/{uid}/expenses/{expenseId}
- users/{uid}/reminders/{reminderId}
- users/{uid}/analytics/{bikeId}

The analytics subcollection stores per-bike business aggregates, distinct from Firebase Analytics telemetry. Initially repository/business logic will maintain aggregates; Cloud Functions is reserved for future scaling. No business writes or profile-document creation are needed for authentication.

Root firestore.rules uses rules version 2 and grants access to users/{uid} and descendants only when request.auth is present, request.auth.uid == uid, and the token has email_verified == true. All other paths are denied. Publish rules manually; adding the file does not deploy it. Add field/schema validation with each future business resource.

Before deployment, check the Rules Playground or emulator: verified owner reads/writes allowed; another UID denied; anonymous and unverified users denied; paths outside users denied. These rules are an ownership baseline, not validation of client-calculated aggregates.

Private Storage objects should live under {uid}/{fileId}, with equivalent owner/verified-email Storage rules before uploads are enabled.

Scope Room records and sync work by Firebase UID. Define retries, conflicts, sign-out behavior, and account switching before implementing local persistence. Do not run another user's queued writes after switching accounts.

Legacy backend accounts/data are not automatically migrated. Existing Firebase accounts in the configured project remain the identity source.

## Verification

Follow the verification policy in `AGENTS.md`.

Run broader Gradle, lint, emulator, or live-Firebase verification only when explicitly requested or required by the task.
Acceptance checklist: registration, verification delivery failure/resend, unverified login/relaunch, foreground verification refresh, verified session restoration, logout/back-stack clearing, hosted reset, invalid credentials, network failure, and throttling. Verify English/Bangla, light/dark, and large-font layouts on a device. Build/unit checks do not validate live email delivery, Console configuration, or deployed rules.
