package mx.unam.contactosapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.unam.contactosapp.data.model.Contact

@Composable
fun LoginScreen(auth: FirebaseAuth, navigateToHome: () -> Unit = {}, navigateToRegister: () -> Unit = {}) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
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
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { emailState.value = it },
            label = { Text("Correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { passwordState.value = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        FirebaseFirestore.getInstance()
                            .collection("contactos")
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

                                navigateToHome()
                            }
                            .addOnFailureListener { e ->
                                Log.e("diego", "Error al cargar contactos: ${e.message}")
                            }
                    } else {
                        Log.i("diego", "LOGIN KO")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navigateToRegister()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }
}