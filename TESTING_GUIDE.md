# Testing Guide

## Current coverage

The project contains only the generated `ExampleUnitTest` (arithmetic) and
`ExampleInstrumentedTest` (package name). There are no splash, repository,
localization, or navigation tests.

JUnit 4, AndroidX JUnit, Espresso, and Compose UI test dependencies are declared.
Coroutine-test, a mocking library, and Hilt testing dependencies are not declared.
Obtain approval before adding test dependencies. Splash previews are useful for
visual inspection but are not automated behavior tests.

## Splash verification priorities

The following is a proposed test plan, not existing coverage:

| Area | Cases to verify |
| --- | --- |
| Initial state | Starts in Loading and initiates authentication once per ViewModel instance |
| Signed-in session | Successful check emits NavigateToHome after the three-second delay |
| Signed-out session | Successful check emits NavigateToLogin after the three-second delay |
| Authentication failure | Error uses the exception message, or the fallback when it is null |
| Retry | Returns to Loading and performs another authentication check |
| Language | Loads saved English/Bangla; missing or unknown tags default to English |
| Screen | Loading shows a progress indicator; Error shows its message and Retry |
| User action | Retry button emits exactly SplashEvent.Retry |
| Navigation | Correct destination selected and splash removed from Back history |

Collect side effects before triggering or advancing the work under test:
the current SharedFlow has zero replay. For coroutine unit tests, use a controlled
Main dispatcher and virtual time once the required testing support is approved.
Avoid real three-second sleeps.

`AuthRepository` is a concrete class depending on FirebaseAuth. Choose a test
boundary or approved mocking approach before writing ViewModel tests; the project
does not already provide a fake auth repository abstraction.
`AppPreferences` can be faked for LanguageManager tests.

## Regression cases for follow-up fixes

- Repeated CheckAuthentication/Retry events should not produce unintended
  duplicate navigation. The current implementation allows overlapping jobs.
- Effects emitted without a collector can be lost. Define the required delivery
  behavior and test collector detach/reattach when fixing it.
- Language initialization currently runs independently and its failure does not
  reach SplashUiState.Error. Define failure/retry behavior before asserting it.
- Verify cancellation during startup work and the splash delay.
- Smoke-test Hilt activity injection and typed route serializer availability.
- Verify localized text and resource changes after splash strings are migrated.

These cases expose current gaps rather than guaranteed behavior. See
[ARCHITECTURE.md](ARCHITECTURE.md) for the source-review findings.

## Commands

From the repository root in PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Instrumented tests require an emulator or connected device. Build tooling, Android
SDK configuration, and dependency resolution must be available. These commands
are guidance; they were not run for this Markdown-only update.

For documentation changes, verify statements against source, local links, package
names, code fences, and the changed-file scope.

## Future data features

When offline storage and synchronization exist, test local-first reads/writes,
queued retry, remote failure, account switching, and uid isolation. Add security
rule tests when Firestore/Storage rules are introduced.
