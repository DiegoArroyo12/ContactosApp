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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.unam.contactosapp.data.model.Contact
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.AppTextField
import mx.unam.contactosapp.ui.components.ErrorDialog
import mx.unam.contactosapp.ui.components.LoadingDialog
import mx.unam.contactosapp.viewmodel.HomeViewModel

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    navigateToHome: () -> Unit = {},
    navigateToRegister: () -> Unit = {},
    homeViewModel: HomeViewModel
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val email = emailState.value
    val password = passwordState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inicia Sesión",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White
        )

        if (isLoading.value) {
            LoadingDialog("Iniciando Sesión")
        }

        errorMessage.value?.let { message ->
            ErrorDialog(message = message) {
                errorMessage.value = null
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Email
        AppTextField(
            value = email,
            onValueChange = { emailState.value = it },
            label = "Correo",
            isEmmail = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        AppTextField(
            value = password,
            onValueChange = { passwordState.value = it },
            label = "Contraseña",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                isLoading.value = true
                errorMessage.value = null
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        FirebaseFirestore.getInstance()
                            .collection("contacts")
                            .whereEqualTo("uidUsuario", uid)
                            .get()
                            .addOnSuccessListener { result ->
                                val contactos = result.map { doc ->
                                    Contact(
                                        name = doc.getString("name") ?: "",
                                        phone = doc.getString("phone") ?: "",
                                        email = doc.getString("email") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        uidUser = doc.getString("uidUsuario") ?: ""
                                    )
                                }

                                Log.i("diego", "Se cargaron ${contactos.size} contactos")

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        val nombre = document.getString("nombre") ?: "Usuario"
                                        Log.i("diego", "Nombre de usuario: $nombre")
                                        homeViewModel.setNombreUsuario(nombre)
                                        navigateToHome()
                                        isLoading.value = false
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage.value = e.message
                                        isLoading.value = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                errorMessage.value = e.message
                                isLoading.value = false
                            }
                    } else {
                        Log.i("diego", "Error: ${task.exception?.message}")
                        errorMessage.value = "Correo o Contraseña Incorrectos"
                        isLoading.value = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppButton(
            onClick = {
                navigateToRegister()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }
}