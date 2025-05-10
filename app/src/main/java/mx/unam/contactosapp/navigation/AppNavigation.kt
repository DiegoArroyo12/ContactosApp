package mx.unam.contactosapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.screens.AddContactScreen
import mx.unam.contactosapp.ui.screens.LoginScreen
import mx.unam.contactosapp.ui.screens.HomeScreen
import mx.unam.contactosapp.ui.screens.RegisterScreen
import mx.unam.contactosapp.viewmodel.HomeViewModel

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    homeViewModel: HomeViewModel
) {

    NavHost(navController = navHostController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                auth = auth,
                homeViewModel = homeViewModel,
                navigateToHome = { navHostController.navigate("home") },
                navigateToRegister = { navHostController.navigate("register?isEditMode=false") }
            )
        }

        composable(
            route = "register?isEditMode={isEditMode}",
            arguments = listOf(
                navArgument("isEditMode") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isEditMode = backStackEntry.arguments?.getBoolean("isEditMode") ?: false
            RegisterScreen(
                auth = auth,
                navigateToLogin = { navHostController.navigate("login") },
                navigateToHome = { navHostController.navigate("home") },
                homeViewModel = homeViewModel,
                isEditMode = isEditMode
            )
        }

        composable("home") {
            HomeScreen(
                navController = navHostController,
                homeViewModel = homeViewModel,
                navigateToAddContact = { contactId ->
                    val route = if (!contactId.isNullOrEmpty()) {
                        "addContact/$contactId"
                    } else {
                        "addContact/null"
                    }
                    navHostController.navigate(route)
                }
            )
        }

        composable(
            route = "addContact/{contactId}",
            arguments = listOf(
                navArgument("contactId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")
            AddContactScreen(
                auth = auth,
                homeViewModel = homeViewModel,
                navigateToHome = { navHostController.navigate("home") },
                contactId = contactId
            )
        }
    }
}