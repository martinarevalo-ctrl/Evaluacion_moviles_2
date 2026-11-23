package com.example.guia11_formularios_validaciones.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.model.Estados
import com.example.guia11_formularios_validaciones.model.Prioridades
import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudEntity
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    val lista by viewModel.listaSolicitudes.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()

    val rol = usuario?.rol
    val titulo = when (rol) {
        Roles.ADMIN -> "Lista de solicitudes"
        Roles.TECNICO -> "Mis solicitudes asignadas"
        else -> "Mis solicitudes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("qr_scanner") }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (rol == Roles.CLIENTE) {
                FloatingActionButton(
                    onClick = {
                        viewModel.resetForm()
                        navController.navigate("solicitud_form")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Solicitud")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Filtros: el administrador filtra por cliente, estado, prioridad y responsable
            if (rol == Roles.ADMIN) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = filtros.cliente,
                        onValueChange = { viewModel.filtrarCliente(it) },
                        label = { Text("Cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListaDesplegable(
                            label = "Estado",
                            valor = filtros.estado,
                            opciones = listOf("") + Estados.todos,
                            onSeleccion = { viewModel.filtrarEstado(it) },
                            modifier = Modifier.weight(1f),
                            etiquetaVacia = "Todos"
                        )
                        ListaDesplegable(
                            label = "Prioridad",
                            valor = filtros.prioridad,
                            opciones = listOf("") + Prioridades.todas,
                            onSeleccion = { viewModel.filtrarPrioridad(it) },
                            modifier = Modifier.weight(1f),
                            etiquetaVacia = "Todas"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ListaDesplegable(
                            label = "Responsable",
                            valor = filtros.responsable,
                            opciones = listOf("") + tecnicos.map { it.nombre },
                            onSeleccion = { viewModel.filtrarResponsable(it) },
                            modifier = Modifier.weight(1f),
                            etiquetaVacia = "Todos"
                        )
                        TextButton(onClick = { viewModel.limpiarFiltros() }) { Text("Limpiar") }
                    }
                }
            }

            if (lista.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "No hay solicitudes para mostrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { solicitud ->
                        SolicitudItemCard(
                            solicitud = solicitud,
                            mostrarCliente = rol != Roles.CLIENTE,
                            onClick = {
                                viewModel.seleccionarSolicitud(solicitud)
                                navController.navigate("solicitud_detalle")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudItemCard(
    solicitud: SolicitudEntity,
    mostrarCliente: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                    Text(
                        text = solicitud.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = solicitud.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = solicitud.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${solicitud.categoria} · Prioridad: ${solicitud.prioridad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = solicitud.fechaHora,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (mostrarCliente) {
                Text(
                    text = "Cliente: ${solicitud.clienteNombre} · Responsable: ${solicitud.tecnicoNombre ?: "Sin asignar"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
