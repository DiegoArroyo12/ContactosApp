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
    val contact:StateFlow<List<Contact>> = _contactos
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    init {
        getContact()
    }

    private fun getContact() {
        viewModelScope.launch {
            val result: List<Contact> = withContext(Dispatchers.IO) {
                getAllContacts()
            }
            _contactos.value = result
        }
    }

    private suspend fun getAllContacts(): List<Contact> {
        return try {
            db.collection("contacts")
                .get()
                .await()
                .documents
                .mapNotNull { snapshot ->
                    snapshot.toObject(Contact::class.java)
                }
        } catch (e: Exception) {
            Log.i("diego", e.toString())
            emptyList()
        }
    }

    fun setNombreUsuario(nombre: String) {
        _nombreUsuario.value = nombre
    }
}