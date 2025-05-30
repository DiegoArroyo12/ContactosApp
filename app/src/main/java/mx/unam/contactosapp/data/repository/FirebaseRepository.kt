package mx.unam.contactosapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import mx.unam.contactosapp.data.model.Contact
import mx.unam.contactosapp.viewmodel.HomeViewModel
import androidx.core.content.edit

class FirebaseRepository() {

    // Crear un nuevo usuario
    fun createUser(
        auth: FirebaseAuth,
        context: Context,
        homeViewModel: HomeViewModel,
        name: String,
        email: String,
        phone: String,
        password: String,
        navigateToHome: () -> Unit,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>
    ) {
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
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }

                            auth.currentUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener {
                                    // Mantener la sesión activa
                                    val sharedPref = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
                                    sharedPref.edit() { putBoolean("rememberUser", true) }

                                    // Cargar sus datos
                                    getContactsUser(auth, homeViewModel, navigateToHome, errorMessage, isLoading)
                                }
                        }
                        .addOnFailureListener { e ->
                            errorMessage.value = e.message
                            isLoading.value = false
                        }
                } else {
                    val exceptionMessage = task.exception?.message
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
    }

    // Editar Usuario
    fun editUser(
        auth: FirebaseAuth,
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
        homeViewModel: HomeViewModel,
        navigateToHome: () -> Unit,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>
    ) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val updatedUserData = mapOf(
                "nombre" to name,
                "correo" to email,
                "telefono" to phone
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(updatedUserData)
                .addOnSuccessListener {
                    // Establecer displayName del usuario actual
                    val profileUpdates = userProfileChangeRequest { displayName = name }
                    auth.currentUser!!.updateProfile(profileUpdates)

                    homeViewModel.setNombreUsuario(name)

                    // Mostrar mensajes informativos después de la navegación
                    val currentEmail = auth.currentUser?.email
                    if (currentEmail != null && currentEmail != email) {
                        auth.currentUser?.verifyBeforeUpdateEmail(email)
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Correo actualizado. Revisa tu bandeja de entrada o Spam.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error al actualizar correo: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                    isLoading.value = false
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    errorMessage.value = e.message
                    isLoading.value = false
                }
        } else {
            errorMessage.value = "No se pudo obtener el usuario actual."
            isLoading.value = false
        }
    }

    // Obtener los contactos de un usuario y el nombre del usuario
    fun getContactsUser(
        auth: FirebaseAuth,
        homeViewModel: HomeViewModel,
        navigateToHome: () -> Unit,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>
    ) {
        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("contacts")
            .whereEqualTo("uidUsuario", uid)
            .get()
            .addOnSuccessListener { result ->
                val contactos = result.map { doc ->
                    Contact(
                        name = doc.getString("nombre") ?: "",
                        phone = doc.getString("telefono") ?: "",
                        email = doc.getString("correo") ?: "",
                        imageUrl = doc.getString("foto") ?: "",
                        uidUser = doc.getString("uidUsuario") ?: "",
                        id = doc.id
                    )
                }
                // Guardamos los contactos
                homeViewModel.setContactos(contactos)
                // Guardar nombre del usuario
                getUserName(auth, homeViewModel, errorMessage, isLoading)
                navigateToHome()
            }
            .addOnFailureListener { e ->
                errorMessage.value = e.message
                isLoading.value = false
            }
    }

    // Obtener Contraseña del usuario
    fun getPassword(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("Usuario no autenticado.")
            return
        }

        val email = user.email
        if (email.isNullOrBlank()) {
            onError("No se encontró el correo electrónico del usuario actual.")
            return
        }

        Log.i("Reauth", "Email: $email") // Para depuración
        Log.i("Reauth", "Password está vacío: ${password.isBlank()}") // Para depuración

        try {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Error de autenticación")
                }
            }
        } catch (e: IllegalArgumentException) {
            onError("Ingresa la contraseña")
        }
    }

    // Recuperar Contraseña
    fun resetPassword(
        auth: FirebaseAuth,
        resetEmail: String,
        errorMessage: MutableState<String?>,
        confirmMessage: MutableState<String?>,
        showResetDialog: MutableState<Boolean>
    ) {
        auth.sendPasswordResetEmail(resetEmail)
            .addOnSuccessListener {
                showResetDialog.value = false
                confirmMessage.value = "Te hemos enviado un correo para restablecer tu contraseña."
            }
            .addOnFailureListener {
                errorMessage.value = "Error al enviar el correo: ${it.message}"
            }
    }

    fun updatePassword(
        auth: FirebaseAuth,
        context: Context,
        newPassword: String,
        confirmNewPassword: String,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>,
        navigateToHome: () -> Unit
    ) {
        if (newPassword == confirmNewPassword && newPassword.length >= 6) {
            isLoading.value = true
            auth.currentUser?.updatePassword(newPassword)
                ?.addOnSuccessListener {
                    isLoading.value = false
                    Toast.makeText(context, "Cambio de Contraseña Exitoso", Toast.LENGTH_LONG).show()
                    navigateToHome()
                }
                ?.addOnFailureListener { e ->
                    isLoading.value = false
                    errorMessage.value = "Error al actualizar la contraseña: ${e.localizedMessage}"
                }
        } else {
            errorMessage.value = "Las contraseñas no coinciden o son muy cortas"
        }
    }

    fun getUserName(
        auth: FirebaseAuth,
        homeViewModel: HomeViewModel,
        errorMessage: MutableState<String?>,
        isLoading: MutableState<Boolean>
    ) {
        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre") ?: "Usuario"
                // Guardamos el nombre del usuario
                homeViewModel.setNombreUsuario(nombre)
                isLoading.value = false
            }
            .addOnFailureListener { e ->
                errorMessage.value = e.message
                isLoading.value = false
            }
    }

    // Rotar la imagen tomada con el teléfono
    fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, android.graphics.Matrix().apply { postRotate(90f) }, true)
            ExifInterface.ORIENTATION_ROTATE_180 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, android.graphics.Matrix().apply { postRotate(180f) }, true)
            ExifInterface.ORIENTATION_ROTATE_270 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, android.graphics.Matrix().apply { postRotate(270f) }, true)
            else -> bitmap
        }
    }

    // Redimensionar la imagen todama con el teléfono
    fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio: Float = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // Editar Contactos
    fun editContactById(
        contactId: String,
        context: Context,
        onName: (String) -> Unit,
        onPhone: (String) -> Unit,
        onEmail: (String) -> Unit,
        onUid: (String) -> Unit = {},
        onImageUri: (Uri) -> Unit = {}
    ) {
        FirebaseFirestore.getInstance().collection("contacts")
            .document(contactId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onName(document.getString("nombre") ?: "")
                    onPhone(document.getString("telefono") ?: "")
                    onEmail(document.getString("correo") ?: "")
                    onUid(document.getString("uidUsuario") ?: "")
                    val base64Image = document.getString("foto")
                    base64Image?.let {
                        val decodedBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        val path = android.provider.MediaStore.Images.Media.insertImage(
                            context.contentResolver,
                            bitmap,
                            "Contact",
                            null
                        )
                        val imageUri = Uri.parse(path)
                        onImageUri(imageUri)
                    }
                } else {
                    Log.e("FirestoreDebug", "No se encontró el contacto")
                }
            }
            .addOnFailureListener{
                Log.e("FirestoreDebug", "Error al obtener el documento: ${it.message}")
            }
    }

    // Eliminar Contactos
    fun deleteContactById(
        contactId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        FirebaseFirestore.getInstance()
            .collection("contacts")
            .document(contactId)
            .delete()
            .addOnSuccessListener {
                Log.i("FirestoreUtils", "Contacto eliminado: $contactId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUtils", "Error al eliminar contacto: ${exception.message}")
                onFailure(exception)
            }
    }
}