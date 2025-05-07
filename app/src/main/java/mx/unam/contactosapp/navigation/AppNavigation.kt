package mx.unam.contactosapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.screens.LoginScreen
import mx.unam.contactosapp.ui.screens.HomeScreen
import mx.unam.contactosapp.ui.screens.RegisterScreen

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {

    NavHost(navController = navHostController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                auth = auth,
                navigateToHome = { navHostController.navigate("home") },
                navigateToRegister = { navHostController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                auth = auth,
                navigateToLogin = { navHostController.navigate("login") }
            )
        }

        composable("home") {
            HomeScreen()
        }
    }
}