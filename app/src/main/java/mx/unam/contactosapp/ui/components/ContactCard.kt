package mx.unam.contactosapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.unam.contactosapp.R
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import mx.unam.contactosapp.data.repository.FirebaseRepository

@Composable
fun ContactCard(
    name: String,
    phone: String,
    email: String,
    imageUrl: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                // Foto de Contacto
                val imageBytes = remember(imageUrl) {
                    try {
                        android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val bitmap = remember(imageBytes) {
                    imageBytes?.let {
                        android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen del contacto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_contact),
                        contentDescription = "Imagen del contacto por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                // Editar Contacto
                IconButton(onClick = { onEdit() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.Gray
                    )
                }

                // Eliminar Contacto
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Gray
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir o Colapsar",
                    tint = Color.Gray
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Teléfono: $phone")
                Text("Correo: $email")
            }
        }
    }
}