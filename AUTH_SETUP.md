# Firebase authentication setup

## Implemented

Login and registration use the existing feature-first MVVM structure and shared
BikeCare theme (warm canvas, orange actions, rounded surfaces, system dark mode).
Both screens support Google sign-in; registration also confirms the password.
The forms include English/Bangla resources, keyboard actions, password autofill
semantics, password visibility, validation, progress, and readable errors.

AuthRepository owns email login, account creation, password reset, and Google
token exchange. Firebase Auth persists the session. Passwords and Google tokens
are not written to DataStore or SavedStateHandle. Credential Manager is invoked
only by the route-level UI boundary.

The approved dependencies are AndroidX credentials and its Play Services adapter
1.3.0, plus Google ID 1.1.1, matching the Firebase Android integration guide.
The Kotlin serialization compiler plugin is enabled for existing typed routes.
MainActivity now has AndroidEntryPoint; the redundant localization provider was
removed.

Successful authentication removes login/registration from the back stack and
opens a temporary Home message. Bike/service data and the real dashboard remain
future work. This change adds no Firestore database, backend functions, or sync queue.

## Required Firebase console configuration

1. Select the Firebase project for the Android application
   `studio.appvero.bikecare`.
2. In Authentication > Sign-in method, enable Email/Password and Google.
   Set Google's support email when prompted.
3. In Project settings > Your apps, register SHA-1 and SHA-256 fingerprints for
   each signing certificate used (debug, release, and Play App Signing when applicable).
   Obtain local fingerprints with `.\gradlew.bat :app:signingReport`.
4. Download the updated `google-services.json` into `app/google-services.json`.
   The current checked-in configuration has no OAuth clients. Google Services
   must generate `default_web_client_id` from a **Web** OAuth client (type 3).
   Do not substitute the Android client ID or invent a client ID.
5. Rebuild and test on a device/emulator with Google Play services and a Google account.

A missing OAuth resource produces a localized setup error and leaves email
authentication usable. Firebase validates Google tokens; the client never treats
an unverified Google token as an authenticated session.

Official reference: https://firebase.google.com/docs/auth/android/google-signin

## Verification

Verified in this workspace: debug APK assembly, 7 passing unit tests (6 auth
validation tests plus the existing template test), lint with no errors, and
Android UI-test compilation. Lint still reports non-blocking dependency/resource
warnings, including the intentional optional OAuth resource lookup. Device tests
were not executed because the emulator launch was declined. Live Firebase login
and Google OAuth were not tested. Language splitting is disabled so installed
app bundles retain English and Bangla for the in-app language switcher.

Run `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
UI interaction tests require an emulator/device:
`.\gradlew.bat :app:connectedDebugAndroidTest`.

Manual checks with configured Firebase:
- Register a fresh email, restart the app, and verify the persisted session.
- Login with valid/invalid credentials; try a duplicate email and a weak password.
- Reset the password using the email field and confirm email delivery.
- Use Google on each screen with existing and new accounts; cancel the picker.
- Verify missing Google configuration, airplane mode, and repeated taps.
- Rotate during email authentication and account selection; background/return.
- Check keyboard scrolling, large text, light/dark mode, and English/Bangla.
- After success, Back must not return to an authentication form.

Unit tests cover input validation; Compose tests cover form actions, busy-state
controls, and password masking. They do not establish live Firebase connectivity.
