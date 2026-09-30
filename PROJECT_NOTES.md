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

Auth routes wait until resumed before navigation. Ignore repeated submissions while busy. Auth-boundary navigation clears the back stack. The authenticated Home shell observes session loss, verification loss, and UID changes across all nested destinations; foreground refresh detects invalidated accounts when online. Logout is available in More.

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

di/FirebaseModule.kt supplies FirebaseAuth and FirebaseFirestore, binds AuthRepository to FirebaseAuthRepository, and binds BikeRepository to FirebaseBikeRepository. Firebase default initialization uses the Google Services configuration.

Inspect Gradle/version files for current dependency versions rather than relying on this document.

If Java is unavailable from PATH, use Android Studio's bundled JBR.

English/Bangla language splitting is disabled.

## Garage and Firestore architecture

Garage lives in features/garage with domain/model/Bike, data/repository, ui/screen, and ui/viewmodel packages. GarageViewModel exposes immutable StateFlow state and retained SharedFlow navigation effects. GarageRoute collects state with lifecycle awareness. GarageScreen is stateless and supports loading, empty, bike list, retryable error, and Add bike states in English/Bangla using existing light/dark theme tokens.

Navigation: Splash waits for Firebase Auth, then selects Login/Register, Verify Email, or Home. Verified sessions enter a Scaffold with Home, Care, Garage, and More. AppNavHost owns a typed nested graph, reusing BikesRoute for Garage, MaintenanceRoute for Care, ProfileRoute for More, and AddBikeRoute. Tab state is saved/restored; Add Bike hides the bottom bar and returns to Garage on Back. The enclosing session guard remains active on Add Bike. Home/Care are placeholders; More provides logout. AddBikeScreen is a placeholder only, without a form or writes.

Firestore data model:
- users/{uid}
- users/{uid}/bikes/{bikeId}
- users/{uid}/fuelLogs/{fuelLogId}
- users/{uid}/maintenanceLogs/{maintenanceId}
- users/{uid}/expenses/{expenseId}
- users/{uid}/reminders/{reminderId}
- users/{uid}/analytics/{bikeId}

Every user-owned document is scoped by Firebase Auth UID. Firebase Auth UID is the ownership boundary; no saved user ID or DataStore flag grants access. Firestore access must always validate authenticated user ownership and verified email. The repository hides Firebase implementation details; UI and ViewModels never access Firestore directly. Domain Bike contains no Firebase types.

Implemented bike documents contain id (matching bikeId), brand, model, year (integer), registrationNumber (empty allowed), initialOdometer and currentOdometer (integer km), imageUrl (nullable HTTPS URL), createdAt and updatedAt (Firestore timestamps), and isActive (boolean). Domain timestamps use epoch milliseconds. Repository add/update use server timestamps; creation time and initial odometer cannot be changed by updates. Bike IDs are caller-supplied stable IDs, limited to 128 characters. Duplicate add and missing update fail rather than silently overwriting/creating records. The odometer must be between the initial value and 10,000,000 km. Inactive bikes remain visible with a text status. Images are stored in the model but image loading/upload is deferred.

FirebaseBikeRepository implements addBike, observeUserBikes, updateBike, and deleteBike using only users/{currentUid}/bikes. Observation orders by createdAt descending; the default descending single-field index is sufficient, with no composite index required. Do not exempt createdAt from indexing. Listener removal follows flow cancellation and account changes. Initial reads wait for a server snapshot (15-second timeout with a retryable network message), so an empty SDK cache is not shown as an authoritative empty Garage. Already displayed data remains visible if connectivity subsequently drops. SDK errors map to domain failures and localized messages; raw exception text is never displayed.

Writes use Firestore transactions and require a network connection, avoiding durable offline write queues until sync is designed. Transaction retries use Firestore SDK behavior, with UID rechecked in each attempt. Concurrent edits use the latest successfully committed mutable values; initial odometer and creation timestamp stay immutable. References are captured under the initiating UID and never redirected to another account. There is no Room cache or custom sync queue yet. Firestore's existing SDK read cache is not used as the authoritative initial list.

Root firestore.rules uses rules version 2, requires request.auth.uid == uid and email_verified == true, and validates bike fields, types, bounds, document ID, and server timestamps. It replaces the previous recursive write grant so validation cannot be bypassed. User profile reads require ownership; profile writes, fuel logs, maintenance logs, expenses, reminders, analytics, and unmatched paths are denied until their schemas/features are implemented. Subcollections can exist without a parent user document. Authentication does not create profile documents.

Publish firestore.rules manually in Firebase Console; adding the file does not deploy it. Before deployment, check the Rules Playground or emulator: verified owner CRUD allowed; another UID, anonymous and unverified users denied; malformed bikes, timestamp tampering, initial-odometer changes, unknown fields, and paths outside bikes denied. Server timestamp writes must be exercised through an SDK. Live Firebase/rules checks require the configured project and are separate from the Android build.

The planned analytics subcollection stores per-bike business aggregates, distinct from Firebase Analytics telemetry. Cloud Functions is reserved for future scaling. No fuel, maintenance, expense, reminder, aggregate, or profile writes are implemented.

## Future persistence

Future offline-first business persistence uses Room where required:
Repository -> Room -> Sync queue -> Firestore.

Private Storage objects should live under {uid}/{fileId}, with equivalent owner/verified-email Storage rules before uploads are enabled.

Scope Room records and sync work by Firebase UID. Define retries, conflicts, sign-out behavior, and account switching before implementing local persistence. Do not run another user's queued writes after switching accounts.

Remaining Garage work: Add Bike form/validation UI, bike details/edit/delete UI, image upload/loading with UID-owned Storage rules, and Room synchronization. No supplied design-reference image was available during the Garage implementation; layout follows existing theme and component conventions.

Legacy backend accounts/data are not automatically migrated. Existing Firebase accounts in the configured project remain the identity source.

## Verification

Follow the verification policy in `AGENTS.md`.

Run broader Gradle, lint, emulator, or live-Firebase verification only when explicitly requested or required by the task.
Acceptance checklist: registration, verification delivery failure/resend, unverified login/relaunch, foreground verification refresh, verified session restoration, logout/back-stack clearing, hosted reset, invalid credentials, network failure, and throttling. Verify English/Bangla, light/dark, and large-font layouts on a device. Build/unit checks do not validate live email delivery, Console configuration, or deployed rules.
