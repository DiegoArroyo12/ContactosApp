package mx.unam.contactosapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import mx.unam.contactosapp.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.ContactCard
import mx.unam.contactosapp.ui.theme.Cancel
import mx.unam.contactosapp.data.repository.FirebaseRepository
import mx.unam.contactosapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    navigateToAddContact: (String?) -> Unit,
    homeViewModel: HomeViewModel
) {
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val nombre by homeViewModel.nombreUsuario.collectAsState()
    val contacts by homeViewModel.contactos.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val confirmLogout = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar = remember { mutableStateOf(false) }
    val confirmDialogVisible = remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = nombre.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("register?isEditMode=true")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings_user),
                            contentDescription = "Editar Usuario",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
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
                    onClick = { confirmLogout.value = true },
                    color = Cancel
                ) {
                    Text(
                        "Cerrar Sesión"
                    )
                }

                // Nuevo Contacto
                AppButton(onClick = { navigateToAddContact(null) }) {
                    Text("Agregar Contacto")
                }
            }
        }
    ) { paddingValues ->
        if (isLoading.value) {
            mx.unam.contactosapp.ui.components.LoadingDialog("Eliminando contacto...")
        }

        errorMessage.value?.let { message ->
            mx.unam.contactosapp.ui.components.ErrorDialog(message = message) {
                errorMessage.value = null
            }
        }

        if (showSnackbar.value) {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Contacto eliminado exitosamente")
                showSnackbar.value = false
            }
        }

        confirmDialogVisible.value?.let { contactIdToDelete ->
            AlertDialog(
                onDismissRequest = { confirmDialogVisible.value = null },
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que deseas eliminar este contacto?") },
                confirmButton = {
                    TextButton(onClick = {
                        isLoading.value = true
                        FirebaseRepository().deleteContactById(contactIdToDelete,
                            onSuccess = {
                                FirebaseRepository().getContactsUser(
                                    auth = FirebaseAuth.getInstance(),
                                    homeViewModel = homeViewModel,
                                    navigateToHome = {},
                                    errorMessage = errorMessage,
                                    isLoading = isLoading
                                )
                                confirmDialogVisible.value = null
                                showSnackbar.value = true
                            },
                            onFailure = {
                                errorMessage.value = it.message
                                isLoading.value = false
                                confirmDialogVisible.value = null
                            }
                        )
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmDialogVisible.value = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Confirm logout dialog
        if (confirmLogout.value) {
            AlertDialog(
                onDismissRequest = { confirmLogout.value = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val sharedPref = navController.context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putBoolean("rememberUser", false).apply()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                        confirmLogout.value = false
                    }) {
                        Text("Cerrar sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmLogout.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar contacto") },
                singleLine = true,
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.search_icon),
                        contentDescription = "Buscar"
                    )
                }
            )

            val filteredContacts = contacts.filter {
                it.name.contains(searchQuery.value, ignoreCase = true) ||
                it.phone.contains(searchQuery.value, ignoreCase = true) ||
                it.email.contains(searchQuery.value, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts.size) { index ->
                    val contact = filteredContacts[index]
                    ContactCard(
                        name = contact.name,
                        phone = contact.phone,
                        email = contact.email,
                        imageUrl = contact.imageUrl,
                        onEdit = { navigateToAddContact(contact.id) },
                        onDelete =  {
                            confirmDialogVisible.value = contact.id
                        }
                    )
                }
            }
        }
    }
}