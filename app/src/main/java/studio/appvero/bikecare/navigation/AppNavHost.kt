package studio.appvero.bikecare.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import studio.appvero.bikecare.features.auth.ui.screen.SplashRoute as SplashContent
import studio.appvero.bikecare.features.auth.ui.screen.LoginRoute as LoginContent
import studio.appvero.bikecare.features.auth.ui.screen.RegisterRoute as RegisterContent
import studio.appvero.bikecare.features.auth.ui.screen.ForgotPasswordRoute as ForgotPasswordContent
import studio.appvero.bikecare.features.auth.ui.screen.VerifyEmailRoute as VerifyEmailContent
import studio.appvero.bikecare.features.auth.ui.screen.HomeRoute as HomeContent
import studio.appvero.bikecare.features.home.ui.screen.HomeScreen
import studio.appvero.bikecare.features.home.ui.screen.HomeTab
import studio.appvero.bikecare.features.home.ui.screen.HomeTabScreen
import studio.appvero.bikecare.features.garage.ui.screen.GarageRoute as GarageContent
import studio.appvero.bikecare.features.garage.ui.screen.AddBikeRoute as AddBikeContent
import studio.appvero.bikecare.features.maintenance.ui.screen.MaintenanceRoute as MaintenanceContent
import studio.appvero.bikecare.features.maintenance.ui.screen.AddMaintenanceRoute as AddMaintenanceContent

@Composable
fun AppNavHost(navController: NavHostController) {
    // Auth-boundary changes replace the whole stack, including registration and recovery.
    fun replaceRoot(destination: Any) {
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashContent(
                onNavigateToHome = { replaceRoot(HomeRoute) },
                onNavigateToLogin = { replaceRoot(LoginRoute) },
                onNavigateToVerifyEmail = { replaceRoot(VerifyEmailRoute()) },
            )
        }
        composable<LoginRoute> {
            LoginContent(
                onNavigateToResetPassword = { navController.navigate(ForgotPasswordRoute) { launchSingleTop = true } },
                onNavigateToHome = { replaceRoot(HomeRoute) },
                onNavigateToVerifyEmail = { replaceRoot(VerifyEmailRoute()) },
                onNavigateToRegister = { navController.navigate(RegisterRoute) { launchSingleTop = true } },
            )
        }
        composable<RegisterRoute> {
            RegisterContent(
                onNavigateToVerifyEmail = { sendEmail -> replaceRoot(VerifyEmailRoute(sendEmail)) },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordContent(onNavigateToLogin = { navController.popBackStack() })
        }
        composable<VerifyEmailRoute> {
            VerifyEmailContent(
                onNavigateToHome = { replaceRoot(HomeRoute) },
                onNavigateToLogin = { replaceRoot(LoginRoute) },
            )
        }
        composable<HomeRoute> {
            HomeContent(
                onNavigateToLogin = { replaceRoot(LoginRoute) },
                onNavigateToVerifyEmail = { replaceRoot(VerifyEmailRoute()) },
            ) { state, onEvent ->
                val homeController = rememberNavController()
                val entry by homeController.currentBackStackEntryAsState()
                val destination = entry?.destination
                val selected = when {
                    destination?.hasRoute<MaintenanceRoute>() == true || destination?.hasRoute<AddMaintenanceRoute>() == true -> HomeTab.Care
                    destination?.hasRoute<BikesRoute>() == true || destination?.hasRoute<AddBikeRoute>() == true -> HomeTab.Garage
                    destination?.hasRoute<ProfileRoute>() == true -> HomeTab.More
                    else -> HomeTab.Home
                }
                HomeScreen(selected, destination?.hasRoute<AddBikeRoute>() != true &&
                    destination?.hasRoute<AddMaintenanceRoute>() != true, onSelectTab = { tab ->
                    val route: Any = when (tab) {
                        HomeTab.Home -> DashboardRoute
                        HomeTab.Care -> MaintenanceRoute
                        HomeTab.Garage -> BikesRoute
                        HomeTab.More -> ProfileRoute
                    }
                    homeController.navigate(route) {
                        popUpTo(homeController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    NavHost(homeController, startDestination = DashboardRoute) {
                        composable<DashboardRoute> { HomeTabScreen(HomeTab.Home, state, onEvent) }
                        composable<MaintenanceRoute> {
                            MaintenanceContent(onNavigateToAddLog = {
                                homeController.navigate(AddMaintenanceRoute) { launchSingleTop = true }
                            })
                        }
                        composable<BikesRoute> {
                            GarageContent(onNavigateToAddBike = { homeController.navigate(AddBikeRoute) { launchSingleTop = true } })
                        }
                        composable<ProfileRoute> { HomeTabScreen(HomeTab.More, state, onEvent) }
                        composable<AddBikeRoute> { AddBikeContent(onNavigateBack = { homeController.popBackStack() }) }
                        composable<AddMaintenanceRoute> {
                            AddMaintenanceContent(onNavigateBack = { homeController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
