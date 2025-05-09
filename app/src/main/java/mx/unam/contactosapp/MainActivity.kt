package mx.unam.contactosapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mx.unam.contactosapp.navigation.AppNavigation
import mx.unam.contactosapp.ui.theme.ContactosAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            navHostController = rememberNavController()
            val currentUser = auth.currentUser
            val context = LocalContext.current
            val sharedPref = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
            val rememberUser = sharedPref.getBoolean("rememberUser", false)

            ContactosAppTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val homeViewModel = (application as ContactosApp).homeViewModel
                    AppNavigation(navHostController, auth, homeViewModel)

                    // Si esta logueado y marco la casilla de recordarme pasa al home
                    LaunchedEffect(currentUser) {
                        if (currentUser != null && rememberUser) {
                            val homeViewModel = (application as ContactosApp).homeViewModel
                            val uid = currentUser.uid

                            navHostController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }

                            homeViewModel.getNombreUsuarioPorUID(uid)
                            val errorMessage = mutableStateOf<String?>(null)
                            val isLoading = mutableStateOf(false)
                            // Recargamos la sesión
                            homeViewModel.reloadSesion(auth, {}, errorMessage, isLoading
                            )
                        }
                    }
                }
            }
        }
    }
}