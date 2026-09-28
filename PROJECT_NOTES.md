# BikeCare setup and known gaps

Read the relevant section when working on startup, authentication, or builds.
Paths below are relative to `app/src/main/java/studio/appvero/bikecare`.

## Startup and authentication

`features/auth/data/repository/AuthRepository.kt` uses Supabase email/password
authentication. Startup awaits SDK session initialization before checking the
current session; this is a local session check, not a separate server validation.
Supabase manages session persistence and refresh. Backup rules include only
DataStore language preferences, excluding the SDK session preferences.
The unused DataStore USER_ID
key does not establish authentication.

Registration enters Home only when Supabase returns an authenticated session.
With Confirm email enabled, it displays confirmation instructions and stays on
the form. Confirm the email in a browser, then sign in with the password.
Supabase can conceal duplicate registrations; the UI does not assume that a
successful signup response always means a newly created account.

`PasswordResetRepository.kt` sends recovery emails and verifies RECOVERY codes
before updating the password. It uses a short-lived client with session storage
disabled, so recovery never signs the main app into Home. After reset, sign in
with the new password. Expired/used codes need a fresh request; process death
restarts the form. No passwords or codes are stored in saved state.

Splash serializes language initialization and auth restoration, supports retry,
preserves cancellation, and retains navigation until acknowledged by its route.
Auth routes wait until resumed before navigating. Repeated submissions are
ignored while busy. Successful login clears the auth back stack.
Home remains temporary success content; sign-out is not implemented.
Google sign-in and Firebase dependencies/configuration have been removed.

## Supabase and local build setup

1. Set `sdk.dir`, `SUPABASE_URL`, and `SUPABASE_PUBLISHABLE_KEY` in ignored
   `local.properties`. Use a publishable key, never a service-role/secret key.
2. Enable the Email provider in Supabase Authentication. Keep Google disabled.
   Configure password requirements and email confirmation as appropriate.
3. Set a valid HTTPS Site URL for the confirmation landing page. The app does
   not consume auth deep links; after confirming, users return and sign in.
4. **Required for password recovery:** edit the Supabase Reset Password email
   template to include `{{ .Token }}`, for example
   `<p>Your BikeCare reset code: {{ .Token }}</p>`.
   Tell recipients to enter it in BikeCare. The default link-only template
   cannot complete this app's recovery form.
5. Configure SMTP/delivery and test confirmation and recovery with real inboxes.
   Dashboard settings and email templates are not changed by this repository.

`di/SupabaseModule.kt` provides a singleton client with Auth, PostgREST, and
Storage using Ktor Android. Kotlin/Compose/serialization use 2.4.0 to read the
existing Supabase 3.8.0 metadata; KSP 2.3.10 supports Kotlin 2.4 module names.
Use Android Studio's bundled JBR as JAVA_HOME when Java is absent from PATH.
Language splitting remains disabled for English/Bangla switching.

No business tables, buckets, repositories, or Firebase business records exist
in this codebase to migrate. Future tables must use user_id ownership with
Postgres RLS (`auth.uid() = user_id`); private Storage buckets need ownership
policies for each operation. Local-first persistence, sync queues, retries,
conflicts, account switching, and cross-user denial tests remain future work.
Existing Firebase accounts are not automatically copied to Supabase.

References: [Supabase signup](https://supabase.com/docs/reference/kotlin/auth-signup),
[recovery](https://supabase.com/docs/reference/kotlin/auth-resetpasswordforemail),
[OTP verification](https://supabase.com/docs/reference/kotlin/auth-verifyotp).

## Verification

From the repository root in PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:connectedDebugAndroidTest
```

The last task requires a device/emulator. Unit tests cover form validation and
Supabase error mapping; Compose tests cover email-only actions, disabled busy
controls, password masking, and recovery fields. They do not verify Supabase
connectivity. Previews include English/Bangla, light/dark, and large text.

Manually verify valid/invalid login, duplicate signup, confirmation on/off,
password policy rejection, email/code delivery, expired/used recovery codes,
password update, session restoration after restart, offline errors, repeated
taps, rotation/backgrounding, and Back after success. Check startup retry and
collector reattachment, keyboard scrolling, both languages/themes, and large
text. Report build, device, and live-service verification separately.
