package mx.unam.contactosapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import mx.unam.contactosapp.data.model.Contact

class HomeViewModel: ViewModel() {

    private var db : FirebaseFirestore = Firebase.firestore
    private val _contactos = MutableStateFlow<List<Contact>>(emptyList())
    val contactos:StateFlow<List<Contact>> = _contactos
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    init {
        listenToContacts()
    }

    private fun listenToContacts() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
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

    fun recargarContactos() {
        listenToContacts()
    }
}