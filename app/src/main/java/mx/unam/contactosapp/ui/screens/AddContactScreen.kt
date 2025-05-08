package mx.unam.contactosapp.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import mx.unam.contactosapp.ui.components.AppButton
import mx.unam.contactosapp.ui.components.AppTextField
import mx.unam.contactosapp.ui.components.ErrorDialog
import mx.unam.contactosapp.ui.components.LoadingDialog
import mx.unam.contactosapp.ui.theme.Cancel
import mx.unam.contactosapp.ui.theme.Photo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

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

@Composable
fun AddContactScreen(
    auth: FirebaseAuth,
    navigateToHome: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var uidUser by remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Cámara e Imágenes
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var file = context.createImageFile()
    var uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "${context.packageName}.fileprovider",
        file
    )
    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        if (it) {
            Toast.makeText(context, "Foto tomada", Toast.LENGTH_SHORT).show()
            capturedImageUri = uri
        } else {
            Toast.makeText(context, "No se pudo tomar la foto $it", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permiso autorizado", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nuevo Contacto",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White
        )

        if (isLoading.value) {
            LoadingDialog("Registrando Contacto...")
        }

        errorMessage.value?.let { message ->
            ErrorDialog(message = message) {
                errorMessage.value = null
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre",
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

        // Email
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo",
            isEmmail = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para capturar imagen
        AppButton(
            onClick = {
                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    val newFile = context.createImageFile()
                    file = newFile
                    uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        newFile
                    )
                    uri?.let { cameraLauncher.launch(it) }
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            color = Photo
        ) {
            Text("Foto")
        }
        if (capturedImageUri != Uri.EMPTY) {
            Image(
                painter = rememberAsyncImagePainter(model = capturedImageUri),
                contentDescription = "Foto del Nuevo Contacto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppButton (
            onClick = {
                isLoading.value = true
                errorMessage.value = null
                val userId = auth.currentUser?.uid ?: return@AppButton
                uidUser = userId

                if (capturedImageUri != Uri.EMPTY) {
                    Log.i("diego", "URI que se va a subir: $capturedImageUri")
                    val inputStream = context.contentResolver.openInputStream(capturedImageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        val resizeBitmap = resizeBitmap(bitmap, 500)
                        val outputStream = ByteArrayOutputStream()
                        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                        // Guardar Contacto en Firestore
                        val db = FirebaseFirestore.getInstance()
                        val contact = hashMapOf(
                            "nombre" to name,
                            "telefono" to phone,
                            "correo" to email,
                            "foto" to base64Image,
                            "uidUsuario" to uidUser
                        )
                        db.collection("contacts").add(contact)
                            .addOnSuccessListener {
                                navigateToHome()
                                isLoading.value = false
                            }
                            .addOnFailureListener { e ->
                                errorMessage.value = e.message
                                isLoading.value = false
                            }
                    } else {
                        errorMessage.value = "No se pudo decodificar la imagen seleccionada."
                        isLoading.value = false
                    }
                } else {
                    errorMessage.value = "Archivo de imagen no encontrado o fue eliminado."
                    isLoading.value = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = navigateToHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Cancel)
        ) {
            Text("Cancelar")
        }
    }
}

fun Context.createImageFile(): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timestamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        cacheDir
    )
}