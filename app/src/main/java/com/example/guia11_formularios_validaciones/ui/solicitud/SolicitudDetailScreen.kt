package com.example.guia11_formularios_validaciones.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.model.Estados
import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetailScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    val solicitud by viewModel.solicitudSeleccionada.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val comentarios by viewModel.comentarios.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()
    val catalogo by viewModel.catalogo.collectAsState()
    val scrollState = rememberScrollState()

    val categorias = (catalogo.servicios.map { it.categoria } + CATEGORIAS_BASE).distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val sol = solicitud
                    val u = usuario
                    // El cliente puede editar su solicitud mientras aún no está asignada
                    val puedeEditar = sol != null && u != null && u.rol == Roles.CLIENTE && sol.clienteUsuario == u.usuario &&
                            sol.estado in listOf(Estados.RECIBIDA, Estados.EN_REVISION, Estados.PENDIENTE_INFO)
                    if (puedeEditar && sol != null) {
                        IconButton(onClick = {
                            viewModel.prepararEdicion(sol)
                            navController.navigate("solicitud_form")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        val sol = solicitud
        if (sol == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text("Cargando...") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Código", style = MaterialTheme.typography.labelMedium)
                        Text(sol.codigo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium) {
                        Text(sol.estado, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(sol.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(sol.descripcion, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(Icons.Default.Category, "Categoría", sol.categoria)
                    InfoRow(Icons.Default.Work, "Servicio", sol.servicio)
                    InfoRow(Icons.Default.Flag, "Prioridad", sol.prioridad)
                    InfoRow(Icons.Default.Person, "Cliente", sol.clienteNombre.ifBlank { "-" })
                    InfoRow(Icons.Default.Person, "Técnico", sol.tecnicoNombre ?: "Sin asignar")
                    InfoRow(Icons.Default.Info, "Fecha Registro", sol.fechaHora)
                    sol.fechaEdicion?.let { InfoRow(Icons.Default.History, "Última actualización", it) }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Evidencia del cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    EvidenciaPreview(sol.evidenciaUri)
                }
            }

            usuario?.let { u ->
                AccionesSolicitud(solicitud = sol, usuario = u, tecnicos = tecnicos, categorias = categorias, viewModel = viewModel)
            }

            // Comentarios y evidencias del seguimiento (el técnico los ve como antecedentes)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Comentarios y evidencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (comentarios.isEmpty()) {
                        Text("Aún no hay comentarios", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                    comentarios.forEach { c ->
                        Column {
                            Text("${c.autor} (${Roles.etiqueta(c.rol)}) · ${c.fechaHora}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            if (c.texto.isNotBlank()) Text(c.texto, style = MaterialTheme.typography.bodyMedium)
                            if (c.evidenciaUri != null) {
                                Spacer(Modifier.height(4.dp))
                                EvidenciaPreview(c.evidenciaUri, mostrarVacia = false)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historial de cambios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    historial.forEach { h ->
                        Column {
                            Text(h.descripcion, style = MaterialTheme.typography.bodyMedium)
                            Text("${h.autor} · ${h.fechaHora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", fontWeight = FontWeight.Bold)
        Text(value)
    }
}
