package studio.appvero.bikecare.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object ResetPasswordRoute

@Serializable
data object HomeRoute

@Serializable
data object BikesRoute

@Serializable
data class BikeDetailRoute(
    val bikeId: String
)

@Serializable
data object AddBikeRoute

@Serializable
data object MaintenanceRoute

@Serializable
data object ProfileRoute
