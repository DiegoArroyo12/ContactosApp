package mx.unam.contactosapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.AppTextField
import mx.unam.contactosapp.ui.components.ErrorDialog
import mx.unam.contactosapp.ui.components.LoadingDialog
import mx.unam.contactosapp.ui.theme.Cancel

@Composable
fun RegisterScreen(auth: FirebaseAuth, navigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading.value) {
            LoadingDialog("Registrando Usuario...")
        }

        errorMessage.value?.let { message ->
            ErrorDialog(message = message) {
                errorMessage.value = null
            }
        }

        Text(
            text = "Regístrate",
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
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                // Validar que se llenen los campos antes de registrar
                if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
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
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            val userData = hashMapOf(
                                "nombre" to name,
                                "correo" to email,
                                "telefono" to phone
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid ?: "")
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.i("diego", "Registro OK y datos guardados en Firestore")
                                    isLoading.value = false
                                    navigateToLogin()
                                }
                                .addOnFailureListener { e ->
                                    errorMessage.value = e.message
                                    isLoading.value = false
                                }
                        } else {
                            val exceptionMessage = task.exception?.message
                            Log.i("diego", "Error: $exceptionMessage")

                            errorMessage.value = when {
                                exceptionMessage?.contains("email address is badly formatted", ignoreCase = true) == true ->
                                    "El formato del correo es inválido."
                                exceptionMessage?.contains("password is invalid", ignoreCase = true) == true ||
                                exceptionMessage?.contains("least 6 characters", ignoreCase = true) == true ->
                                    "La contraseña debe tener al menos 6 caracteres."
                                exceptionMessage?.contains("email address is already in use", ignoreCase = true) == true ->
                                    "El correo ya está registrado."
                                else -> "No se pudo registrar el nuevo usuario. Intenta nuevamente más tarde."
                            }
                            isLoading.value = false
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        AppButton(
            onClick = navigateToLogin,
            modifier = Modifier.fillMaxWidth(),
            color = Cancel
        ) {
            Text("Cancelar")
        }
    }
}