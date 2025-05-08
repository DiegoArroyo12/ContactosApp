package mx.unam.contactosapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.ContactCard
import mx.unam.contactosapp.ui.theme.Cancel
import mx.unam.contactosapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    navigateToAddContact: () -> Unit,
    homeViewModel: HomeViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    val nombre by homeViewModel.nombreUsuario.collectAsState()
                    Text(
                        text = "Contactos de ${nombre.ifBlank { "Usuario" }}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logout
                AppButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    color = Cancel
                ) {
                    Text(
                        "Cerrar Sesión"
                    )
                }

                // New Contact
                AppButton(onClick = { navigateToAddContact() }) {
                    Text("Agregar Contacto")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar contacto") }
            )

            val contacts by homeViewModel.contact.collectAsState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts.size) { index ->
                    val contact = contacts[index]
                    ContactCard(
                        name = contact.name,
                        phone = contact.phone,
                        correo = contact.email,
                        imageUrl = contact.imageUrl
                    )
                }
            }
        }
    }
}