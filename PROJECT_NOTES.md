# BikeCare — Project Context

This file is the concise source of truth for Codex when working on BikeCare. Read the relevant sections before changing authentication, navigation, Garage, Firebase configuration, persistence, or build setup. Inspect the code and Gradle files for the actual implementation and dependency versions; this document records decisions and known state, not a substitute for source code.

## Product and architecture

BikeCare is an Android motorcycle-care app for managing bikes, fuel, maintenance, expenses, reminders, and related insights.

- UI: Kotlin, Jetpack Compose, Material 3.
- Architecture: feature-oriented MVVM with repository interfaces and Clean Architecture boundaries; Hilt for dependency injection; Kotlin Coroutines and Flow.
- Identity: Firebase Authentication.
- Cloud business data: Cloud Firestore.
- Media: Firebase Storage is selected; uploads are not implemented.
- Preferences: DataStore, for app settings such as language only.
- MVP offline behavior: Firestore's built-in persistent cache and queued writes. Room and a custom synchronization layer are deferred.
- Firebase Analytics SDK is included; custom event instrumentation and business aggregates are not implemented. Crashlytics is deferred.

Keep Firebase SDK types inside data/repository and DI code. ViewModels and UI use domain models and repository abstractions. Follow the existing feature folders and conventions; do not add use-case or presentation layers unless the codebase adopts them consistently.

## Current implementation

### Authentication and startup

- Flow: Screen → ViewModel → `AuthRepository` interface → `FirebaseAuthRepository`.
- `AuthUser` contains UID, email, and verification status. Repository errors are SDK-independent; UI maps them to English/Bangla text.
- Email/password is the only enabled sign-in method. Google sign-in is deferred.
- Firebase Authentication owns identity, session persistence, and token refresh. DataStore IDs or flags never establish authentication.
- Startup waits for the first auth-state callback, then routes to Login/Register, Verify Email, or Home. Splash initializes language and auth, supports retry, preserves coroutine cancellation, and retains navigation effects until acknowledged.
- Registration creates the account once, then navigates to Verify Email. That screen sends the initial verification message independently; delivery failure leaves the account available for resend.
- Unverified sign-ins and restored unverified sessions go to Verify Email. Refresh reloads the Firebase user and refreshes the ID token before entering Home; foreground return triggers refresh. Network errors remain retryable.
- Password reset sends a Firebase-hosted link and uses neutral account-existence messaging. The user chooses a new password on the hosted page and signs in normally. No recovery code or Android app link is required.
- Never persist passwords or auth tokens in saved state. Auth routes wait until resumed before navigating and ignore repeated submissions while busy. Auth-boundary navigation clears the back stack.
- The authenticated Home shell observes session loss, verification loss, and UID changes across nested destinations. Foreground refresh detects invalidated accounts when online. Logout is available from More.

### Navigation and Garage

- Verified sessions enter a `Scaffold` with Home, Care, Garage, and More tabs.
- `AppNavHost` owns the typed nested graph for Garage, Care, More, Add Bike, and Add Maintenance. Tab state is saved/restored. Add forms hide the bottom bar and return to their tabs on Back; the session guard remains active.
- Home remains a placeholder. Care shows the 100 most recent maintenance logs and opens an add form. More provides logout.
- Garage lives under `features/garage` with domain/model, data/repository, and UI screen/ViewModel code. `GarageViewModel` exposes immutable `StateFlow` state and retained `SharedFlow` navigation effects. The route collects state lifecycle-aware; the screen is stateless.
- Garage supports loading, empty, bike-list, retryable-error, and Add Bike states in English/Bangla using existing theme tokens.
- Implemented bike operations: add, observe, update, delete. Details/edit/delete screens, Add Bike form and validation, image upload/display, and user-facing pending/rejected-write handling remain future work.

## Firebase setup and operational constraints

- Use the existing Firebase project's Android app for package `studio.appvero.bikecare`; its configuration belongs at `app/google-services.json`.
- Enable Firebase Authentication Email/Password. Keep Google sign-in deferred. Configure email enumeration protection, the password policy, verification/reset templates, sender/support details, and authorized Firebase-hosted action domain. The app validates at least six characters at signup; Firebase enforces the configured policy. App language support does not automatically localize Firebase email templates.
- Create Firestore in production mode in the selected region. Publish and verify root `firestore.rules` in Firebase Console before allowing user data. A local rules file is not deployed automatically.
- Default Firebase initialization uses Google Services configuration. `di/FirebaseModule.kt` provides `FirebaseAuth` and `FirebaseFirestore`, and binds `AuthRepository` to `FirebaseAuthRepository` and `BikeRepository` to `FirebaseBikeRepository`.
- `local.properties` is for Android SDK configuration (`sdk.dir`); backend credentials are not read from it. Never embed service-account credentials in the app.
- Firebase Storage SDK is included, but uploads and bucket activation are deferred. Cloud Storage requires Blaze billing even when usage is within no-cost allowances; before enabling billing, set budget alerts (they do not cap spending). Configure Crashlytics separately if/when introduced.
- If Java is unavailable on PATH, use Android Studio's bundled JBR. English/Bangla language splitting is disabled.
- Legacy backend accounts/data are not automatically migrated. Existing accounts in the configured Firebase project remain the identity source.

## Firestore architecture

Design for the main access boundary and access pattern: an authenticated user reads and manages their own bikes and records, usually scoped to one bike and ordered by date. Every user-owned document is beneath `users/{uid}`. Firebase Auth UID is authoritative; never accept a client-supplied owner UID or a saved ID as proof of access.

### Collection layout

```text
users/{uid}                                      # profile and account preferences
  bikes/{bikeId}                                 # implemented
  fuelLogs/{fuelLogId}                           # planned
  maintenanceLogs/{maintenanceLogId}             # implemented
  expenses/{expenseId}                           # planned
  reminders/{reminderId}                         # planned
  analytics/{bikeId}                             # planned per-bike derived summary
    months/{yyyy-MM}                              # planned monthly derived aggregate
```

The user document is the profile document; do not add a redundant `profile` subcollection. Firestore subcollections may exist even if the parent `users/{uid}` document has not been created. Authentication does not currently create profile documents. No subscription/billing schema is committed; add one only with a concrete product requirement.

Each planned record that belongs to a bike stores `bikeId` as a scalar reference to `users/{uid}/bikes/{bikeId}`. It is not a cross-user Firestore reference. Keep records in their user-scoped top-level subcollections for simple user-wide and per-bike queries; denormalize a small display field only when a demonstrated screen/query needs it. Deleting or archiving a bike must have an explicit policy for its logs and aggregates; Firestore does not cascade-delete subcollections.

### Current bike document

Path: `users/{uid}/bikes/{bikeId}`. The document ID equals its `id` field and is a caller-supplied stable ID (maximum 128 characters).

```json
{
  "id": "stable-bike-id",
  "brand": "Yamaha",
  "model": "FZ-S FI",
  "year": 2023,
  "registrationNumber": "",
  "initialOdometer": 5000,
  "currentOdometer": 12500,
  "imageUrl": null,
  "createdAt": "Firestore Timestamp",
  "updatedAt": "Firestore Timestamp",
  "isActive": true
}
```

`year` and odometers are integers; odometers are kilometers. Registration number may be empty. Image URL is nullable and, when present, must be HTTPS. Firestore stores timestamps; domain models expose epoch milliseconds. Writes use server timestamps. `createdAt` and `initialOdometer` are immutable after creation. Odometer must be between the initial value and 10,000,000 km. Inactive bikes remain visible with a text status. Images are represented in the model, but image loading/upload is deferred.

### Planned feature records

These are target schemas except for Maintenance, which is implemented as described below. Before implementing another feature, define its Kotlin model, validation, write semantics, indexes, and matching rules together. Prefer explicit fields and bounded documents; do not store an unbounded history in a single document.

- **Profile — `users/{uid}`:** optional display name, photo reference, preferred currency, locale, and `createdAt`/`updatedAt`. Firebase Auth remains authoritative for email and verification. Store only fields the product needs; profile writes are currently denied by rules.
- **Fuel — `fuelLogs/{fuelLogId}`:** `id`, `bikeId`, `occurredAt`, `odometerKm`, `volumeLiters`, `totalCostMinor`, `currencyCode`, `station` (optional), `isFullTank`, `note` (optional), `createdAt`, `updatedAt`. Use integer minor currency units (for example, paisa) to avoid floating-point money. Define fuel-efficiency calculation rules before using partial fills; do not infer a full-tank interval from incomplete data.
- **Maintenance — `maintenanceLogs/{maintenanceLogId}` (implemented):** The document ID is the log ID. Fields are `bikeId`, `title`, `category`, `serviceType`, `date` (Timestamp), `odometer`, `cost` (whole BDT), `provider`, `description`, nullable `nextServiceOdometer`, `nextServiceDate` (Timestamp), `receiptUrl` (currently null), and server `createdAt`. Reads show the 100 most recent logs ordered by `date`; writes are create-only and may remain pending offline. Receipt uploads, edit/delete, and pagination are not implemented.
- **Expenses — `expenses/{expenseId}`:** `id`, `bikeId`, `categoryId`, `occurredAt`, `amountMinor`, `currencyCode`, `note` (optional), optional `maintenanceLogId`/`fuelLogId` when linked, `createdAt`, `updatedAt`. Avoid double-counting linked records in totals by defining one source of truth per expense category.
- **Reminders — `reminders/{reminderId}`:** `id`, `bikeId`, `title`, `kind`, optional `dueAt` and/or `dueOdometerKm`, `status`, `completedAt` (optional), optional source maintenance record ID, `createdAt`, `updatedAt`. Local notifications can be scheduled on-device; delivery must not depend on an unimplemented server job.
- **Per-bike analytics — `analytics/{bikeId}`:** rebuildable summary keyed by bike ID, with calculation/version metadata, update time, and only bounded headline values needed by dashboard screens (for example, total cost and latest known odometer). Treat it as derived data, not the source of truth.
- **Monthly aggregates — `analytics/{bikeId}/months/{yyyy-MM}`:** one document per bike and calendar month, with month key/timezone policy, currency-separated totals by category, event counts, and calculation version/update time. Do not combine currencies into one total. Define whether month boundaries use the user's configured timezone before implementation.

For planned date-based records, `occurredAt` represents the event time and `createdAt`/`updatedAt` represent persistence metadata. Current maintenance logs use `date` for the event day and server `createdAt` for persistence metadata. Use server timestamps for persistence metadata; allow client event dates only after validation. Validate finite positive volumes, nonnegative costs, valid currency codes, reasonable date/odometer bounds, and required fields at the UI/domain boundary and again in security rules where feasible. IDs must be stable, non-sequential, and no longer than Firestore's 1,500-byte document-ID limit; current bikes retain their existing caller-supplied ID contract. Never use mutable names as IDs.

### Ownership, rules, and validation

- All client reads/writes must use the signed-in UID's path. Firestore Rules require `request.auth != null`, `request.auth.uid == uid`, and `request.auth.token.email_verified == true` for user data. The app refreshes the ID token after verification before entering Home; a stale token can be denied until refreshed.
- Root `firestore.rules` uses rules version 2. Current rules allow verified owners to read their user document, validate bike CRUD, and read/create maintenance logs for an existing active bike under the same UID. Profile writes, fuel logs, expenses, reminders, analytics, and unmatched paths remain denied. Rules must be extended narrowly as each feature ships; never add a recursive catch-all write grant.
- Validate allowed keys, types, required values, bounds, path/document ID consistency, immutable fields, and server timestamps. Rules are the server-side authority. Client validation improves feedback but cannot authoritatively detect duplicates, missing remote records, or concurrent changes while offline.
- A bike's parent ownership does not automatically validate a related log's `bikeId`. When rules are extended, verify the referenced bike exists under the same UID and define behavior for inactive/deleted bikes. Keep rule document lookups bounded and account for their access-call limits.
- Unverified/anonymous users, other UIDs, malformed documents, forbidden fields, and unimplemented paths must be denied. Never display raw Firebase exception text or log credentials, tokens, or email addresses.
- Rules Playground or Emulator checks should cover verified-owner CRUD; other UID; anonymous and unverified requests; malformed/unknown fields; ID mismatch; timestamp tampering; immutable-field changes; paths outside enabled collections; and SDK server-timestamp writes. Console publication and live Firebase checks are separate from an Android build.

### IDs, timestamps, and writes

- Current bike IDs remain caller-supplied stable values. For future append-only log records, prefer Firestore auto IDs or UUIDs rather than sequential IDs. Store IDs in the document only when application/domain code needs them; if stored, require equality with the document ID.
- Use Firestore `Timestamp` for instants. Represent calendar month aggregates with a documented `yyyy-MM` key plus an explicit timezone/month-boundary policy. Keep money as integer minor units plus ISO 4217 currency code.
- Create with server `createdAt` and `updatedAt`; update `updatedAt` with server time and preserve immutable creation fields. For client-generated event times, distinguish event time from server persistence time.
- Firestore transactions/batches are for bounded atomic operations, not large histories. Avoid client-side read-modify-write aggregates that can lose concurrent updates. MVP screens can calculate small summaries from queried source records; add server-maintained aggregates only when access volume or cost justifies them.

### Query patterns and indexes

Design queries around the screens, use bounded pages for histories, and add indexes from actual query shapes. Firestore's normal single-field indexes are enabled unless deliberately exempted.

| Screen / access pattern | Query shape | Index guidance |
| --- | --- | --- |
| Garage bikes | `users/{uid}/bikes`, order by `createdAt` descending | Current default single-field index is sufficient; do not exempt `createdAt`. |
| Care maintenance list | `users/{uid}/maintenanceLogs`, order by `date` descending, limit 100 | Default single-field index on `date` is sufficient. |
| Recent fuel/maintenance/expenses for one bike | Filter `bikeId == selectedBikeId`, order by `occurredAt` descending, limit/page | Composite index for `bikeId` plus `occurredAt` descending; create from deployed query requirements. |
| User-wide history | Order a record collection by `occurredAt` descending, optionally filter category/status | Single-field index for ordering; add composite index for each combined filter/order shape. |
| Upcoming reminders | Filter active status and due date (and/or bike), order by due date | Composite index matching equality filters and due-time ordering. Use separate queries if combining due date and odometer creates awkward semantics. |
| Dashboard/month view | Read `analytics/{bikeId}` and the requested `months/{yyyy-MM}` document | Direct document reads; no collection query index required. |

Commit Firestore index configuration alongside the feature if the repository uses it. Do not blindly create every possible index; query operators, equality fields, and sort directions determine the needed composite index. Paginate long histories with document cursors and limits. Never fetch every user's data or scan an unbounded collection from the client.

### Offline persistence and account switching

- `FirebaseModule` explicitly configures Firestore persistent local cache. Listeners include metadata changes and emit cached snapshots immediately. Cache-only empty results may be incomplete until the server is reached.
- Regular Firestore writes update the local view optimistically and queue for sync. Repository completion means submitted to the SDK, not accepted by the server. Rules rejection or conflicting server state can later roll back the local mutation; UI should eventually expose pending/rejected state for write screens.
- Listener cancellation follows Flow cancellation and account changes. Capture the initiating UID and never redirect queued writes or references to another account after switching users. Rules remain the access authority on the server.
- Do not add Room or a custom sync queue for MVP. Reconsider only if the product needs richer local queries, explicit sync/conflict state, or persistence guarantees beyond Firestore's cache. Any future local business records and sync work must be UID-scoped.

### Cloud Functions and derived data (future)

Cloud Functions are available as a future scaling option, not an MVP dependency. Do not make current writes depend on them. If server-maintained analytics, reminders, or cleanup is added:

- Use authenticated, least-privilege server code and validate inputs again; clients must not write trusted aggregates directly.
- Make Firestore-triggered work idempotent because events may be retried or delivered more than once. Store source event/version markers or recompute bounded aggregates safely.
- Keep source logs authoritative and aggregates rebuildable. Handle edits/deletes, duplicate events, currency separation, timezone/month boundaries, and backfills explicitly.
- Use scheduled functions only when server-side timing is actually required. Set billing alerts and assess plan requirements before enabling functions; do not promise no-cost operation without checking current Firebase plan/region usage.

### Firebase Storage (future uploads)

Store private media under the established UID-scoped pattern `{uid}/{fileId}`. Keep object paths in Firestore; do not store image bytes in documents. Use unguessable file IDs, owner/verified-email Storage Rules, size and content-type allowlists, and validate that the referenced bike belongs to the same UID. Prefer Storage paths over long-lived public URLs for private objects; create download URLs only when needed. Define replacement/deletion cleanup and orphan handling before uploads ship. Storage activation requires explicit billing setup as noted above.

## MVP status at a glance

| Area | Status |
| --- | --- |
| Email/password auth, verification, reset, session guard, logout | Implemented; verify against source before changes |
| Home shell and bottom navigation | Implemented; Home remains a placeholder |
| Bikes in Firestore, repository, Garage list states, offline cache | Implemented |
| Add Bike screen | Placeholder only; no form or writes |
| Bike detail/edit/delete UI and pending-write UX | Planned |
| Profile document writes | Denied/not implemented |
| Maintenance | Care list and add form, Firestore repository and local rules implemented; rules still require deployment |
| Fuel, expenses, reminders | Planned; no collections/writes implemented |
| Per-bike and monthly business aggregates | Planned; distinct from Firebase Analytics telemetry |
| Cloud Functions | Deferred; not required for MVP |
| Storage uploads and image loading | Deferred; SDK included |
| Room/custom synchronization | Deferred; Firestore built-in persistence is used now |
| Crashlytics/custom telemetry | Deferred/not configured |

## Verification guidance

Follow `AGENTS.md` for the project's verification policy. Do not run broader Gradle, lint, emulator, or live-Firebase verification unless explicitly requested or required by the task.

When authentication is changed, acceptance coverage includes registration, verification delivery failure/resend, unverified login/relaunch, foreground verification refresh, verified session restoration, logout/back-stack clearing, hosted password reset, invalid credentials, network failure, and throttling. Check English/Bangla, light/dark, and large-font layouts on a device when requested. Build/unit checks do not validate live email delivery, Firebase Console setup, Storage setup, or deployed security rules.

When Garage/Firestore is changed, check loading/empty/list/error behavior, cache-first rendering, offline queued writes, asynchronous rule rejection/rollback, UID switching, and rule behavior in the Emulator/Rules Playground as appropriate. Live checks require the configured Firebase project and deployed rules.

## Known follow-up work

Complete bike detail/edit/delete flows; image loading and UID-owned Storage rules; and user-facing treatment for rejected offline writes. Add maintenance pagination, receipt uploads, and edit/delete when needed. Define feature schemas/rules/indexes when adding fuel, expenses, reminders, profiles, or aggregates.
