package com.example.camara.ui.view

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.camara.ui.viewmodel.QrViewModel
import com.example.camara.utils.CameraPermissionHelper
import com.example.camara.utils.QrScanner
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    solicitudViewModel: SolicitudViewModel,
    navController: NavController,
    hasCameraPermission: Boolean = CameraPermissionHelper.hasCameraPermission(LocalContext.current),
    onRequestPermission: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasCameraPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Se necesita permiso de cámara para escanear QR", Toast.LENGTH_LONG).show()
        }
    }

    val requestPermissionAction = onRequestPermission ?: {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Limpiar resultado al entrar y solicitar permiso si no se ha otorgado
    LaunchedEffect(Unit) {
        viewModel.clearResult()
        if (!permissionGranted) {
            requestPermissionAction()
        }
    }

    val qrResult by viewModel.qrResult.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escáner de Solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!permissionGranted) {
                Text("Permiso de cámara requerido", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                Button(onClick = requestPermissionAction) {
                    Text("Conceder permiso")
                }
            } else if (qrResult == null) {
                Text("Enfoca el código QR de la solicitud", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    QrScanner(
                        onQrCodeScanned = { qrContent ->
                            viewModel.onQrDetected(qrContent)
                            solicitudViewModel.buscarPorCodigo(
                                codigo = qrContent,
                                onEncontrada = {
                                    Toast.makeText(context, "Solicitud encontrada", Toast.LENGTH_SHORT).show()
                                    navController.navigate("solicitud_detalle") {
                                        // Evitar que al volver del detalle se quede en el QR con el resultado
                                        popUpTo("qr_scanner") { inclusive = true }
                                    }
                                },
                                onNoEncontrada = {
                                    Toast.makeText(context, "Código no encontrado: $qrContent", Toast.LENGTH_LONG).show()
                                    viewModel.clearResult() // Re-activar escaneo
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        modifier = Modifier.size(250.dp).align(Alignment.Center),
                        color = Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {}
                }
            } else {
                // Estado intermedio si la navegación tarda o falla
                CircularProgressIndicator()
                Text("Procesando código...", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
