package mx.unam.contactosapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var user by mutableStateOf<FirebaseUser?>(null)

    fun login(onSuccess: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    errorMessage = null
                    onSuccess()
                } else {
                    errorMessage = task.exception?.message
                }
            }
    }

    fun register(onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    errorMessage = null
                    onSuccess()
                } else {
                    errorMessage = task.exception?.message
                }
            }
    }

    fun logout() {
        auth.signOut()
        user = null
    }
}