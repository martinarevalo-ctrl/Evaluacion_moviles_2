package com.example.guia11_formularios_validaciones.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

// Avisos locales: nueva solicitud, asignación, cambio de estado y resolución pendiente de confirmar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(navController: NavController, viewModel: SolicitudViewModel) {
    val avisos by viewModel.avisos.collectAsState()

    // Se marcan como leídos al salir de la pantalla, así se alcanzan a ver como nuevos
    DisposableEffect(Unit) {
        onDispose { viewModel.marcarAvisosLeidos() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        if (avisos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No tienes avisos", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avisos, key = { it.id }) { aviso ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (aviso.leido) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(aviso.mensaje, fontWeight = if (aviso.leido) FontWeight.Normal else FontWeight.Bold)
                            Text(aviso.fechaHora, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
