package mx.unam.contactosapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.unam.contactosapp.data.repository.FirebaseRepository
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.AppTextField
import mx.unam.contactosapp.ui.components.ErrorDialog
import mx.unam.contactosapp.ui.components.LoadingDialog
import mx.unam.contactosapp.ui.theme.Cancel
import mx.unam.contactosapp.ui.theme.Photo
import mx.unam.contactosapp.viewmodel.HomeViewModel

@Composable
fun RegisterScreen(auth: FirebaseAuth, navigateToLogin: () -> Unit, navigateToHome: () -> Unit, homeViewModel: HomeViewModel, isEditMode: Boolean) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showNewPasswordDialog by remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            val user = auth.currentUser
            user?.uid?.let { uid ->
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        name = document.getString("nombre") ?: ""
                        email = document.getString("correo") ?: ""
                        phone = document.getString("telefono") ?: ""
                    }
                    .addOnFailureListener { e ->
                        errorMessage.value = "No se pudieron cargar los datos del usuario: ${e.message}"
                    }
            }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading.value) {
            LoadingDialog(if (isEditMode) "Actualizando Usuario..." else "Registrando Usuario...")
        }

        errorMessage.value?.let { message ->
            ErrorDialog(message = message) {
                errorMessage.value = null
            }
        }

        Text(
            text = if (isEditMode) "Editar Usuario" else "Regístrate",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo",
            isEmmail = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone
        AppTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Teléfono",
            isPhone = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        if (isEditMode) {
            var showPasswordDialog by remember { mutableStateOf(false) }

            AppButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                color = Photo
            ) {
                Text("Cambiar contraseña")
            }

            // Confirmar contraseña
            if (showPasswordDialog) {
                var currentPassword by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text("Confirmar contraseña actual") },
                    text = {
                        AppTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = "Contraseña actual",
                            isPassword = !passwordVisible,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                Icon(
                                    imageVector = icon,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                                )
                            },
                            colorText = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    confirmButton = {
                        AppButton(
                            onClick = {
                                FirebaseRepository().getPassword(
                                    password = currentPassword,
                                    onSuccess = {
                                        showPasswordDialog = false
                                        showNewPasswordDialog = true
                                    },
                                    onError = {
                                        errorMessage.value = it
                                    }
                                )
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        AppButton(
                            onClick = { showPasswordDialog = false },
                            color = Cancel
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Nueva contraseña
            if (showNewPasswordDialog) {
                var newPassword by remember { mutableStateOf("") }
                var confirmNewPassword by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showNewPasswordDialog = false },
                    title = { Text("Cambiar contraseña") },
                    text = {
                        Column {
                            AppTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "Nueva contraseña",
                                isPassword = !passwordVisible,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                                    )
                                },
                                colorText = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AppTextField(
                                value = confirmNewPassword,
                                onValueChange = { confirmNewPassword = it },
                                label = "Confirmar contraseña",
                                isPassword = !passwordVisible,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                                    )
                                },
                                colorText = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    confirmButton = {
                        val context = LocalContext.current
                        AppButton(
                            onClick = {
                                FirebaseRepository().updatePassword(
                                    auth = auth,
                                    context = context,
                                    newPassword = newPassword,
                                    confirmNewPassword = confirmNewPassword,
                                    errorMessage = errorMessage,
                                    isLoading = isLoading,
                                    navigateToHome = navigateToHome
                                )
                            }
                        ) {
                            Text("Actualizar")
                        }
                    },
                    dismissButton = {
                        AppButton(
                            onClick = { showNewPasswordDialog = false },
                            color = Cancel
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        } else {
            var passwordVisible by remember { mutableStateOf(false) }
            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                isPassword = !passwordVisible,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.White,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                // Validar que se llenen los campos antes de registrar
                if (name.isBlank() || email.isBlank() || phone.isBlank() || (!isEditMode && password.isBlank())) {
                    errorMessage.value = "Por favor completa todos los campos antes de registrarte."
                    return@AppButton
                }

                // Validar correo
                val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
                if (!emailRegex.matches(email)) {
                    errorMessage.value = "Ingresa un correo válido."
                    return@AppButton
                }

                // Validar teléfono
                if (!phone.matches(Regex("^\\d{10}$"))) {
                    errorMessage.value = "Ingresa un número de teléfono válido de 10 dígitos."
                    return@AppButton
                }

                isLoading.value = true
                errorMessage.value = null
                if (isEditMode) {
                    // Actualizar Usuario
                    FirebaseRepository().editUser(auth, context, name, email, phone, password, homeViewModel, navigateToHome, errorMessage, isLoading)
                } else {
                    // Crear Usuario
                    FirebaseRepository().createUser(auth, name, email, phone, password, navigateToLogin, errorMessage, isLoading)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditMode) "Actualizar" else "Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        AppButton(
            onClick = { if (isEditMode) navigateToHome() else navigateToLogin() },
            modifier = Modifier.fillMaxWidth(),
            color = Cancel
        ) {
            Text("Cancelar")
        }
    }
}