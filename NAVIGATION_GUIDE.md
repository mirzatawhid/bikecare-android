# Navigation Guide

## Current graph

Navigation is centralized in `app/src/main/java/studio/appvero/bikecare/navigation`.

- `AppRoute.kt` declares serializable destination objects/data classes.
- `AppNavHost.kt` registers destinations and navigation callbacks.
- `BikeCareApp.kt` creates the controller using `rememberNavController()`.

| Destination | Registration |
| --- | --- |
| `SplashRoute` | Start destination; renders the auth splash route composable |
| `LoginRoute` | Registered with an empty body |
| `HomeRoute` | Registered with an empty body |
| `RegisterRoute`, `BikesRoute`, `BikeDetailRoute(bikeId)`, `AddBikeRoute`, `MaintenanceRoute`, `ProfileRoute` | Declared only; not registered |

A declaration alone does not create a working destination.

## Destination types versus route composables

There are two splash symbols with different responsibilities:

- `navigation.SplashRoute`: the serializable destination object.
- `features.auth.ui.screen.SplashRoute`: the composable that connects the
  ViewModel to the screen.

Use clear imports or aliases if these names become ambiguous. Preserve typed
navigation rather than constructing string route names.

## Side-effect flow

`SplashViewModel` emits `NavigateToHome` or `NavigateToLogin`.
The route composable collects `sideEffect` in `LaunchedEffect(Unit)` and invokes
`onNavigateToHome` or `onNavigateToLogin`. `AppNavHost` supplies those callbacks
and executes navigation:

```kotlin
navController.navigate(HomeRoute) {
    popUpTo<SplashRoute> {
        inclusive = true
    }
}
```

Login navigation uses the same splash removal. This prevents Back from returning
to splash after a successful transition. The screen and ViewModel do not own a
`NavController`.

The route collects UI state with lifecycle awareness, but its side-effect
collection currently uses a composition-scoped `LaunchedEffect`. These are
different collection lifetimes. The zero-replay SharedFlow does not retain
navigation for a future collector when no collector is present.

## Adding a destination

1. Add an `@Serializable` destination to `AppRoute.kt`; use a data class when
   arguments are needed, following `BikeDetailRoute(val bikeId: String)`.
2. Register it with `composable<Destination>` in `AppNavHost`.
3. Render the feature route composable and supply navigation callbacks.
4. Keep navigation decisions as ViewModel side effects and back-stack operations
   in the graph.
5. Verify argument handling, effect delivery, and Back behavior.

The serialization JSON library is declared, but the Kotlin serialization compiler
plugin is missing from the inspected build configuration. Resolve serializer
generation before runtime verification of typed navigation. Also replace empty
Home/Login content before treating startup navigation as a complete user flow.
