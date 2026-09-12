package studio.appvero.bikecare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import studio.appvero.bikecare.features.auth.ui.screen.SplashRoute

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
            // LoginRoute(...)
        }

        composable<HomeRoute> {
            // HomeRoute(...)
        }
    }
}