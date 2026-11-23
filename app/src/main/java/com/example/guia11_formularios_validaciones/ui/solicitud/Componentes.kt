package com.example.guia11_formularios_validaciones.ui.solicitud

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

val CATEGORIAS_BASE = listOf("Infraestructura", "Soporte Técnico", "Limpieza", "Seguridad", "Otros")

// Lista desplegable de solo lectura. Una opción vacía ("") se muestra con `etiquetaVacia` (por ejemplo "Todos")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDesplegable(
    label: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiquetaVacia: String = "",
    error: String? = null
) {
    var expandido by remember { mutableStateOf(false) }
    fun mostrar(texto: String) = if (texto.isEmpty()) etiquetaVacia else texto

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }, modifier = modifier) {
        OutlinedTextField(
            value = mostrar(valor),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            isError = error != null,
            supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(mostrar(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

// Foto (cámara) o documento adjunto como evidencia. Guarda una copia en el almacenamiento de la app
@Composable
fun SelectorEvidencia(
    evidenciaUri: String?,
    onEvidenciaChange: (String?) -> Unit,
    titulo: String = "Evidencia (foto o documento)"
) {
    val context = LocalContext.current

    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onEvidenciaChange(guardarFoto(context, bitmap))
    }
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) camaraLauncher.launch(null)
        else Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
    }
    val documentoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val ruta = copiarDocumento(context, uri)
            if (ruta != null) onEvidenciaChange(ruta)
            else Toast.makeText(context, "No se pudo adjuntar el documento", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            EvidenciaPreview(evidenciaUri, mostrarVacia = false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        camaraLauncher.launch(null)
                    } else {
                        permisoLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Foto")
                }
                OutlinedButton(onClick = { documentoLauncher.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Documento")
                }
            }
            if (evidenciaUri != null) {
                TextButton(onClick = { onEvidenciaChange(null) }) { Text("Quitar evidencia") }
            }
        }
    }
}

// Muestra la imagen si el archivo es una foto; si es otro tipo de documento muestra su nombre
@Composable
fun EvidenciaPreview(ruta: String?, mostrarVacia: Boolean = true) {
    val archivo = remember(ruta) { ruta?.let { File(it) }?.takeIf { it.exists() } }
    val bitmap = remember(archivo) {
        archivo?.let { runCatching { BitmapFactory.decodeFile(it.absolutePath) }.getOrNull() }
    }

    when {
        bitmap != null -> Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Evidencia",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
        )
        archivo != null -> Text("📎 ${archivo.name}", style = MaterialTheme.typography.bodyMedium)
        mostrarVacia -> Text("Sin evidencia adjunta", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
    }
}

private fun guardarFoto(context: Context, bitmap: Bitmap): String? = try {
    val archivo = File(context.filesDir, "evidencia_${System.currentTimeMillis()}.jpg")
    FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    archivo.absolutePath
} catch (e: Exception) {
    null
}

private fun copiarDocumento(context: Context, uri: Uri): String? {
    return try {
        var nombre = "documento"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val columna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columna >= 0 && cursor.moveToFirst()) nombre = cursor.getString(columna) ?: nombre
        }
        val destino = File(context.filesDir, "doc_${System.currentTimeMillis()}_$nombre")
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        inputStream.use { entrada ->
            FileOutputStream(destino).use { salida -> entrada.copyTo(salida) }
        }
        destino.absolutePath
    } catch (e: Exception) {
        null
    }
}
