package com.example.guia11_formularios_validaciones.ui.solicitud

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudEntity
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val catalogo by viewModel.catalogo.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var creada by remember { mutableStateOf<SolicitudEntity?>(null) }

    // Las categorías salen del catálogo externo (Firebase) y de las categorías base
    val categorias = (catalogo.servicios.map { it.categoria } + CATEGORIAS_BASE).distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Solicitud" else "Nueva Solicitud") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetForm()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ErrorAnimatedTextField(
                value = uiState.titulo,
                onValueChange = { viewModel.onTituloChanged(it) },
                label = "Título *",
                error = uiState.tituloError
            )

            ErrorAnimatedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = "Descripción *",
                error = uiState.descripcionError,
                supportingText = "(mínimo 10 caracteres)",
                minLines = 3
            )

            // Servicio: se elige del catálogo externo de servicios informáticos (Firebase)
            val opcionesServicio = catalogo.servicios.map { it.servicio }
            ListaDesplegable(
                label = "Servicio (catálogo externo) *",
                valor = uiState.servicio,
                opciones = opcionesServicio,
                onSeleccion = { nombre ->
                    val elegido = catalogo.servicios.firstOrNull { it.servicio == nombre }
                    viewModel.onServicioCatalogoElegido(nombre, elegido?.categoria ?: uiState.categoria)
                },
                modifier = Modifier.fillMaxWidth(),
                etiquetaVacia = "Selecciona un servicio",
                error = uiState.servicioError
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (catalogo.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Cargando catálogo...", style = MaterialTheme.typography.bodySmall)
                } else {
                    catalogo.mensaje?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = { viewModel.cargarCatalogo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Recargar catálogo")
                    }
                }
            }

            uiState.servicio.takeIf { it.isNotBlank() }?.let { nombre ->
                catalogo.servicios.firstOrNull { it.servicio == nombre }?.descripcion?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            ListaDesplegable(
                label = "Categoría *",
                valor = uiState.categoria,
                opciones = categorias,
                onSeleccion = { viewModel.onCategoriaChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                error = uiState.categoriaError
            )

            SelectorEvidencia(
                evidenciaUri = uiState.evidenciaUri,
                onEvidenciaChange = { viewModel.onEvidenciaUriChanged(it) }
            )

            Button(
                onClick = {
                    viewModel.validarYGuardar { nueva ->
                        if (nueva != null) {
                            creada = nueva
                        } else {
                            Toast.makeText(context, "Solicitud actualizada", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando
            ) {
                Text(if (isEditing) "Actualizar Solicitud" else "Guardar Solicitud")
            }
        }
    }

    creada?.let { solicitud ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Solicitud registrada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu código único es:")
                    Text(solicitud.codigo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("Estado: ${solicitud.estado}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { compartirPorWhatsApp(context, solicitud) }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir por WhatsApp (simulado)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    creada = null
                    navController.popBackStack()
                }) { Text("Listo") }
            }
        )
    }
}

// Comparte la referencia de la solicitud. Intenta WhatsApp y, si no está instalado, muestra el selector de apps
private fun compartirPorWhatsApp(context: android.content.Context, solicitud: SolicitudEntity) {
    val texto = "Solicitud ${solicitud.codigo}: ${solicitud.titulo}\nEstado: ${solicitud.estado}\nSigue tu solicitud en la app de Servicios Digitales."
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    try {
        context.startActivity(Intent(intent).setPackage("com.whatsapp"))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent.createChooser(intent, "Compartir referencia"))
    }
}

@Composable
fun ErrorAnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    supportingText: String = "",
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        minLines = minLines,
        singleLine = minLines == 1,
        supportingText = {
            Column {
                AnimatedVisibility(
                    visible = error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                if (supportingText.isNotEmpty() && error == null) {
                    Text(supportingText)
                }
            }
        }
    )
}
