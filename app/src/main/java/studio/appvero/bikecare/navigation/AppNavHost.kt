package studio.appvero.bikecare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import studio.appvero.bikecare.features.auth.ui.screen.SplashRoute
import studio.appvero.bikecare.features.auth.ui.screen.LoginRoute as LoginContent
import studio.appvero.bikecare.features.auth.ui.screen.RegisterRoute as RegisterContent
import studio.appvero.bikecare.features.auth.ui.screen.AuthSuccessScreen

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {

        composable<SplashRoute> {

            SplashRoute(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo<SplashRoute> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<SplashRoute> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginContent(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute) { launchSingleTop = true }
                },
            )
        }

        composable<RegisterRoute> {
            RegisterContent(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable<HomeRoute> {
            AuthSuccessScreen()
        }
    }
}
