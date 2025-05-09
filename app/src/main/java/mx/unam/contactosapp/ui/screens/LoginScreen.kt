package mx.unam.contactosapp.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.google.firebase.auth.FirebaseAuth
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.AppTextField
import mx.unam.contactosapp.ui.components.ErrorDialog
import mx.unam.contactosapp.ui.components.LoadingDialog
import mx.unam.contactosapp.data.repository.FirebaseRepository
import mx.unam.contactosapp.viewmodel.HomeViewModel
import androidx.core.content.edit
import mx.unam.contactosapp.ui.components.AppCheckBox

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

    val rememberMe = remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

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

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { rememberMe.value = !rememberMe.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckBox(
                checked = rememberMe.value,
                onCheckedChange = { rememberMe.value = it },
            )
            Text("Recordarme", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                // Validar que se llenen los campos antes de registrar
                if (email.isBlank() || password.isBlank()) {
                    errorMessage.value = "Por favor ingresa tu correo y contraseña "
                    return@AppButton
                }

                isLoading.value = true
                errorMessage.value = null
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        if (rememberMe.value) {
                            val sharedPref = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
                            sharedPref.edit() { putBoolean("rememberUser", true) }
                        }
                        FirebaseRepository().getContactsUser(auth, homeViewModel, navigateToHome, errorMessage, isLoading)
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