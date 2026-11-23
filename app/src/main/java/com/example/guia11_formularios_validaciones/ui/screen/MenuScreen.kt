package com.example.guia11_formularios_validaciones.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.util.Notificador
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController, viewModel: SolicitudViewModel) {
    val usuario by viewModel.usuario.collectAsState()
    val noLeidos by viewModel.avisosNoLeidos.collectAsState()
    val context = LocalContext.current

    // Permiso para mostrar avisos como notificaciones (Android 13+)
    val permisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Aviso local cuando hay avisos sin leer
    LaunchedEffect(noLeidos) {
        if (noLeidos > 0) {
            Notificador.mostrar(context, "Solicitudes de servicios", "Tienes $noLeidos aviso(s) sin leer")
        }
    }

    val rol = usuario?.rol

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú Principal") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            usuario?.let {
                Text("Hola, ${it.nombre}", style = MaterialTheme.typography.titleLarge)
                Text(Roles.etiqueta(it.rol), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }

            if (rol == Roles.CLIENTE) {
                BotonMenu("Nueva solicitud", { Icon(Icons.Default.Add, contentDescription = null) }) {
                    viewModel.resetForm()
                    navController.navigate("solicitud_form")
                }
            }

            BotonMenu(
                when (rol) {
                    Roles.ADMIN -> "Lista de solicitudes"
                    Roles.TECNICO -> "Mis solicitudes asignadas"
                    else -> "Mis solicitudes / seguimiento"
                },
                { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
            ) { navController.navigate("lista_solicitudes") }

            if (rol == Roles.ADMIN) {
                BotonMenu("Panel resumen", { Icon(Icons.Default.Dashboard, contentDescription = null) }) {
                    navController.navigate("panel_resumen")
                }
            }

            BotonMenu("Escanear código QR", { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }) {
                navController.navigate("qr_scanner")
            }

            BotonMenu("Catálogo API & DAO (GET)", { Icon(Icons.Default.CloudDownload, contentDescription = null) }) {
                navController.navigate("catalogo_api")
            }

            Button(
                onClick = { navController.navigate("avisos") },
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                BadgedBox(badge = { if (noLeidos > 0) Badge { Text("$noLeidos") } }) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text("Avisos")
            }
        }
    }
}

@Composable
private fun BotonMenu(texto: String, icono: @Composable () -> Unit, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(60.dp)) {
        icono()
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}
