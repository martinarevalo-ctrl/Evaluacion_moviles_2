package com.example.guia11_formularios_validaciones.ui.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.guia11_formularios_validaciones.model.Estados
import com.example.guia11_formularios_validaciones.model.Prioridades
import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudEntity
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioEntity
import com.example.guia11_formularios_validaciones.viewmodel.SolicitudViewModel

private enum class Dialogo { REVISAR, INFO, ASIGNAR, CANCELAR, AVANCE, RESOLUCION, RESPUESTA, REAPERTURA }

// Acciones disponibles según el perfil y el estado actual de la solicitud
@Composable
fun AccionesSolicitud(
    solicitud: SolicitudEntity,
    usuario: UsuarioEntity,
    tecnicos: List<UsuarioEntity>,
    categorias: List<String>,
    viewModel: SolicitudViewModel
) {
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }
    val estado = solicitud.estado
    if (Estados.finalizado(estado)) return

    val esDuenio = usuario.rol == Roles.CLIENTE && solicitud.clienteUsuario == usuario.usuario
    val esTecnicoAsignado = usuario.rol == Roles.TECNICO && solicitud.tecnicoUsuario == usuario.usuario

    val botones = mutableListOf<Pair<String, () -> Unit>>()

    when {
        usuario.rol == Roles.ADMIN -> {
            if (estado == Estados.RECIBIDA) botones += "Revisar y clasificar" to { dialogo = Dialogo.REVISAR }
            if (estado == Estados.RECIBIDA || estado == Estados.EN_REVISION)
                botones += "Solicitar información adicional" to { dialogo = Dialogo.INFO }
            if (estado == Estados.RECIBIDA || estado == Estados.EN_REVISION || estado == Estados.ASIGNADA)
                botones += "Definir prioridad y asignar técnico" to { dialogo = Dialogo.ASIGNAR }
            if (estado == Estados.RESUELTA)
                botones += "Cerrar solicitud" to { viewModel.cerrarSolicitud(solicitud.id) }
            botones += "Cancelar solicitud" to { dialogo = Dialogo.CANCELAR }
        }
        esTecnicoAsignado -> {
            if (estado == Estados.ASIGNADA) botones += "Iniciar atención" to { viewModel.iniciarAtencion(solicitud.id) }
            if (estado == Estados.EN_PROCESO) {
                botones += "Registrar avance y evidencias" to { dialogo = Dialogo.AVANCE }
                botones += "Proponer resolución" to { dialogo = Dialogo.RESOLUCION }
            }
        }
        esDuenio -> {
            if (estado == Estados.PENDIENTE_INFO) botones += "Responder solicitud de información" to { dialogo = Dialogo.RESPUESTA }
            if (estado == Estados.RESUELTA) {
                botones += "Confirmar resolución" to { viewModel.confirmarResolucion(solicitud.id) }
                botones += "Pedir reapertura" to { dialogo = Dialogo.REAPERTURA }
            }
        }
    }

    if (botones.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Acciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            botones.forEach { (texto, accion) ->
                Button(onClick = accion, modifier = Modifier.fillMaxWidth()) { Text(texto) }
            }
        }
    }

    val cerrar = { dialogo = null }
    when (dialogo) {
        Dialogo.REVISAR -> DialogoRevisar(solicitud.categoria, categorias, onConfirmar = {
            viewModel.revisarYClasificar(solicitud.id, it); cerrar()
        }, onCancelar = cerrar)

        Dialogo.INFO -> DialogoTexto("Solicitar información adicional", "¿Qué información necesitas del cliente?", "Enviar", onConfirmar = { texto, _ ->
            viewModel.solicitarInformacion(solicitud.id, texto); cerrar()
        }, onCancelar = cerrar)

        Dialogo.ASIGNAR -> DialogoAsignar(solicitud.prioridad, tecnicos, onConfirmar = { prioridad, tecnico ->
            viewModel.asignarTecnico(solicitud.id, prioridad, tecnico); cerrar()
        }, onCancelar = cerrar)

        Dialogo.CANCELAR -> DialogoTexto("Cancelar solicitud", "Motivo de la cancelación", "Cancelar solicitud", onConfirmar = { texto, _ ->
            viewModel.cancelarSolicitud(solicitud.id, texto); cerrar()
        }, onCancelar = cerrar)

        Dialogo.AVANCE -> DialogoTexto("Registrar avance", "Describe el avance", "Guardar", conEvidencia = true, onConfirmar = { texto, evidencia ->
            viewModel.registrarAvance(solicitud.id, texto, evidencia); cerrar()
        }, onCancelar = cerrar)

        Dialogo.RESOLUCION -> DialogoTexto("Proponer resolución", "Explica cómo se resolvió", "Proponer", conEvidencia = true, onConfirmar = { texto, evidencia ->
            viewModel.proponerResolucion(solicitud.id, texto, evidencia); cerrar()
        }, onCancelar = cerrar)

        Dialogo.RESPUESTA -> DialogoTexto("Responder al coordinador", "Información solicitada", "Enviar", conEvidencia = true, onConfirmar = { texto, evidencia ->
            viewModel.responderInformacion(solicitud.id, texto, evidencia); cerrar()
        }, onCancelar = cerrar)

        Dialogo.REAPERTURA -> DialogoTexto("Pedir reapertura", "¿Por qué no quedó resuelta?", "Pedir reapertura", onConfirmar = { texto, _ ->
            viewModel.pedirReapertura(solicitud.id, texto); cerrar()
        }, onCancelar = cerrar)

        null -> {}
    }
}

@Composable
private fun DialogoTexto(
    titulo: String,
    etiqueta: String,
    textoConfirmar: String,
    conEvidencia: Boolean = false,
    onConfirmar: (String, String?) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var evidencia by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it; error = false },
                    label = { Text(etiqueta) },
                    minLines = 3,
                    isError = error,
                    supportingText = { if (error) Text("Este campo es obligatorio", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (conEvidencia) {
                    SelectorEvidencia(evidenciaUri = evidencia, onEvidenciaChange = { evidencia = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (texto.isBlank()) error = true else onConfirmar(texto.trim(), evidencia)
            }) { Text(textoConfirmar) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Volver") } }
    )
}

@Composable
private fun DialogoRevisar(categoriaActual: String, categorias: List<String>, onConfirmar: (String) -> Unit, onCancelar: () -> Unit) {
    var categoria by remember { mutableStateOf(categoriaActual) }
    val opciones = (listOf(categoriaActual) + categorias).distinct()

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Revisar y clasificar") },
        text = {
            ListaDesplegable(label = "Categoría", valor = categoria, opciones = opciones, onSeleccion = { categoria = it }, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { Button(onClick = { onConfirmar(categoria) }) { Text("Pasar a revisión") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Volver") } }
    )
}

@Composable
private fun DialogoAsignar(
    prioridadActual: String,
    tecnicos: List<UsuarioEntity>,
    onConfirmar: (String, UsuarioEntity) -> Unit,
    onCancelar: () -> Unit
) {
    var prioridad by remember { mutableStateOf(if (prioridadActual in Prioridades.asignables) prioridadActual else "Media") }
    var tecnicoNombre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Prioridad y técnico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListaDesplegable(label = "Prioridad", valor = prioridad, opciones = Prioridades.asignables, onSeleccion = { prioridad = it }, modifier = Modifier.fillMaxWidth())
                ListaDesplegable(
                    label = "Técnico",
                    valor = tecnicoNombre,
                    opciones = tecnicos.map { it.nombre },
                    onSeleccion = { tecnicoNombre = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    etiquetaVacia = "Selecciona un técnico",
                    error = error
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val elegido = tecnicos.firstOrNull { it.nombre == tecnicoNombre }
                if (elegido == null) error = "Debes elegir un técnico" else onConfirmar(prioridad, elegido)
            }) { Text("Asignar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Volver") } }
    )
}
