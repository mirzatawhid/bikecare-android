package studio.appvero.bikecare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import studio.appvero.bikecare.features.auth.ui.screen.SplashRoute as SplashContent
import studio.appvero.bikecare.features.auth.ui.screen.LoginRoute as LoginContent
import studio.appvero.bikecare.features.auth.ui.screen.RegisterRoute as RegisterContent
import studio.appvero.bikecare.features.auth.ui.screen.ForgotPasswordRoute as ForgotPasswordContent
import studio.appvero.bikecare.features.auth.ui.screen.VerifyEmailRoute as VerifyEmailContent
import studio.appvero.bikecare.features.auth.ui.screen.HomeRoute as HomeContent

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
            )
        }
    }
}