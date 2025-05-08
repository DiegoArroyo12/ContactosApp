package mx.unam.contactosapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.screens.AddContactScreen
import mx.unam.contactosapp.ui.screens.LoginScreen
import mx.unam.contactosapp.ui.screens.HomeScreen
import mx.unam.contactosapp.ui.screens.RegisterScreen
import mx.unam.contactosapp.viewmodel.HomeViewModel

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navHostController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                auth = auth,
                homeViewModel = homeViewModel,
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
            HomeScreen(
                navController = navHostController,
                homeViewModel = homeViewModel,
                navigateToAddContact = { navHostController.navigate("addContact") }
            )
        }

        composable("addContact") {
            AddContactScreen(
                auth = auth,
                navigateToHome = { navHostController.navigate("home") }
            )
        }
    }
}