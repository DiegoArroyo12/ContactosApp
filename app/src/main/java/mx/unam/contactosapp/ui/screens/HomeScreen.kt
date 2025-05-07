package mx.unam.contactosapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.unam.contactosapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = HomeViewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Small Top App Bar") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { /* TODO: Navegar a agregar contacto */ }) {
                    Text("Agregar Contacto")
                }
//                Button(onClick = { onLogout }) {
//                    Text("Cerrar Sesión")
//                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            // Lista simulada de contactos
            repeat(5) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nombre $index", style = MaterialTheme.typography.titleLarge)
                        Text("Teléfono: 555-1234$index")
                        Text("Correo: contacto$index@example.com")
                    }
                }
            }
        }
    }
}