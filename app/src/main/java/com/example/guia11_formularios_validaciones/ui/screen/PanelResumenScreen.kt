package com.example.guia11_formularios_validaciones.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

// Panel resumen del administrador: supervisa el avance de todas las solicitudes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelResumenScreen(navController: NavController, viewModel: SolicitudViewModel) {
    val resumen by viewModel.resumen.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel resumen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaNumero("Total", resumen.total, Modifier.weight(1f))
                TarjetaNumero("Sin asignar", resumen.sinAsignar, Modifier.weight(1f))
            }

            TarjetaLista("Solicitudes por estado", resumen.porEstado)
            TarjetaLista("Solicitudes por prioridad", resumen.porPrioridad)
            TarjetaLista(
                "Carga activa por técnico",
                resumen.cargaTecnicos,
                vacio = "Ningún técnico tiene solicitudes activas"
            )
        }
    }
}

@Composable
private fun TarjetaNumero(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text("$valor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TarjetaLista(titulo: String, datos: Map<String, Int>, vacio: String = "Sin datos") {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (datos.isEmpty()) Text(vacio, style = MaterialTheme.typography.bodyMedium)
            datos.forEach { (clave, cantidad) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(clave)
                    Text("$cantidad", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
