package mx.unam.contactosapp.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.unam.contactosapp.data.model.Contact
import mx.unam.contactosapp.data.repository.FirebaseRepository

class HomeViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
): ViewModel() {

    private var db : FirebaseFirestore = Firebase.firestore
    private val _contactos = MutableStateFlow<List<Contact>>(emptyList())
    val contactos:StateFlow<List<Contact>> = _contactos
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    init {
        listenToContacts()
    }

    private fun listenToContacts() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _contactos.value = emptyList()
            return
        }

        db.collection("contacts")
            .whereEqualTo("uidUsuario", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("HomeViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val contactos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Contact::class.java)?.copy(id = doc.id)
                    }
                    _contactos.value = contactos
                } else {
                    _contactos.value = emptyList()
                }
            }
    }

    fun setNombreUsuario(nombre: String) {
        _nombreUsuario.value = nombre
    }

    fun setContactos(contactos: List<Contact>) {
        _contactos.value = contactos
    }

    fun reloadSesion(
        auth: FirebaseAuth,
        navigateToHome: () -> Unit,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>
    ) {
        repository.getContactsUser(
            auth = auth,
            homeViewModel = this,
            navigateToHome = {},
            errorMessage = errorMessage,
            isLoading = isLoading
        )
    }

    fun getNombreUsuarioPorUID(uid: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"
                    _nombreUsuario.value = nombre
                    Log.i("HomeViewModel", "Nombre de usuario: $nombre")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al obtener el nombre del usuario", e)
            }
        }
    }
}