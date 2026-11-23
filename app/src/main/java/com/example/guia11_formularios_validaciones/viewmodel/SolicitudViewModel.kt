package com.example.guia11_formularios_validaciones.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guia11_formularios_validaciones.model.CatalogoUiState
import com.example.guia11_formularios_validaciones.model.Estados
import com.example.guia11_formularios_validaciones.model.FiltrosUiState
import com.example.guia11_formularios_validaciones.model.Prioridades
import com.example.guia11_formularios_validaciones.model.ResumenUi
import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.model.SolicitudUiState
import com.example.guia11_formularios_validaciones.model.data.local.AppDatabase
import com.example.guia11_formularios_validaciones.model.data.local.AvisoEntity
import com.example.guia11_formularios_validaciones.model.data.local.ComentarioEntity
import com.example.guia11_formularios_validaciones.model.data.local.HistorialEntity
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudEntity
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioEntity
import com.example.guia11_formularios_validaciones.model.data.remote.ApiClient
import com.example.guia11_formularios_validaciones.model.data.repository.CatalogoRepository
import com.example.guia11_formularios_validaciones.model.data.repository.SolicitudRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SolicitudRepository
    private val catalogoRepository: CatalogoRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SolicitudRepository(db.solicitudDao(), db.usuarioDao(), db.seguimientoDao())
        catalogoRepository = CatalogoRepository(ApiClient.catalogoApi, db.catalogoDao())
    }

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)

    // ---------- Sesión ----------
    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario.asStateFlow()

    fun iniciarSesion(usuario: UsuarioEntity) {
        _usuario.value = usuario
        _filtros.value = FiltrosUiState()
        _seleccionadaId.value = null
        resetForm()
    }

    fun cerrarSesion() {
        _usuario.value = null
        _seleccionadaId.value = null
        resetForm()
    }

    // ---------- Solicitudes (visibles según el perfil) ----------
    private val todas: StateFlow<List<SolicitudEntity>> = repository.listaSolicitudes.estado(emptyList())

    private val visibles = combine(todas, _usuario) { lista, u ->
        when (u?.rol) {
            Roles.ADMIN -> lista
            Roles.TECNICO -> lista.filter { it.tecnicoUsuario == u.usuario }
            Roles.CLIENTE -> lista.filter { it.clienteUsuario == u.usuario }
            else -> emptyList()
        }
    }

    private val _filtros = MutableStateFlow(FiltrosUiState())
    val filtros: StateFlow<FiltrosUiState> = _filtros.asStateFlow()

    val listaSolicitudes: StateFlow<List<SolicitudEntity>> = combine(visibles, _filtros) { lista, f ->
        lista.filter { s ->
            (f.cliente.isBlank() || s.clienteNombre.contains(f.cliente.trim(), ignoreCase = true) ||
                    s.clienteUsuario.contains(f.cliente.trim(), ignoreCase = true)) &&
                    (f.estado.isBlank() || s.estado == f.estado) &&
                    (f.prioridad.isBlank() || s.prioridad == f.prioridad) &&
                    (f.responsable.isBlank() || s.tecnicoNombre == f.responsable)
        }
    }.estado(emptyList())

    fun filtrarCliente(valor: String) = _filtros.update { it.copy(cliente = valor) }
    fun filtrarEstado(valor: String) = _filtros.update { it.copy(estado = valor) }
    fun filtrarPrioridad(valor: String) = _filtros.update { it.copy(prioridad = valor) }
    fun filtrarResponsable(valor: String) = _filtros.update { it.copy(responsable = valor) }
    fun limpiarFiltros() { _filtros.value = FiltrosUiState() }

    val tecnicos: StateFlow<List<UsuarioEntity>> = repository.tecnicos.estado(emptyList())

    // Panel resumen del administrador
    val resumen: StateFlow<ResumenUi> = todas.map { lista ->
        ResumenUi(
            total = lista.size,
            sinAsignar = lista.count { it.tecnicoUsuario == null && !Estados.finalizado(it.estado) },
            porEstado = Estados.todos.associateWith { e -> lista.count { it.estado == e } },
            porPrioridad = Prioridades.todas.associateWith { p -> lista.count { it.prioridad == p } },
            cargaTecnicos = lista
                .filter { it.tecnicoNombre != null && !Estados.finalizado(it.estado) }
                .groupingBy { it.tecnicoNombre!! }
                .eachCount()
        )
    }.estado(ResumenUi())

    // ---------- Solicitud seleccionada, historial y comentarios ----------
    private val _seleccionadaId = MutableStateFlow<Long?>(null)

    val solicitudSeleccionada: StateFlow<SolicitudEntity?> = combine(todas, _seleccionadaId) { lista, id ->
        lista.firstOrNull { it.id == id }
    }.estado(null)

    val historial: StateFlow<List<HistorialEntity>> = _seleccionadaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observarHistorial(id)
    }.estado(emptyList())

    val comentarios: StateFlow<List<ComentarioEntity>> = _seleccionadaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observarComentarios(id)
    }.estado(emptyList())

    fun seleccionarSolicitud(solicitud: SolicitudEntity?) {
        _seleccionadaId.value = solicitud?.id
    }

    // ---------- Avisos locales ----------
    private fun destinatarios(u: UsuarioEntity) = listOf(u.usuario, "rol:${u.rol}")

    val avisos: StateFlow<List<AvisoEntity>> = _usuario.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else repository.observarAvisos(destinatarios(u))
    }.estado(emptyList())

    val avisosNoLeidos: StateFlow<Int> = avisos.map { lista -> lista.count { !it.leido } }.estado(0)

    fun marcarAvisosLeidos() {
        val u = _usuario.value ?: return
        viewModelScope.launch { repository.marcarAvisosLeidos(destinatarios(u)) }
    }

    // ---------- Catálogo externo (Firebase) ----------
    private val _catalogo = MutableStateFlow(CatalogoUiState())
    val catalogo: StateFlow<CatalogoUiState> = _catalogo.asStateFlow()

    // Va después de declarar _catalogo para que ya esté inicializado cuando arranca la carga
    init {
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        viewModelScope.launch {
            _catalogo.update { it.copy(cargando = true, mensaje = null) }
            catalogoRepository.obtenerServiciosDeApi()
                .onSuccess { servicios ->
                    _catalogo.value = CatalogoUiState(servicios = servicios)
                }
                .onFailure {
                    val locales = catalogoRepository.obtenerServiciosLocales()
                    if (locales.isNotEmpty() && locales != CatalogoRepository.respaldo) {
                        _catalogo.value = CatalogoUiState(
                            servicios = locales,
                            mensaje = "Objetos obtenidos desde la base de datos local (DAO)."
                        )
                    } else {
                        _catalogo.value = CatalogoUiState(
                            servicios = CatalogoRepository.respaldo,
                            mensaje = "No se pudo conectar a Firebase. Se muestra un catálogo de respaldo."
                        )
                    }
                }
        }
    }

    // ---------- Formulario ----------
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isProcessingQr = MutableStateFlow(false)
    val isProcessingQr: StateFlow<Boolean> = _isProcessingQr.asStateFlow()

    private var idEdicion: Long? = null

    fun buscarPorCodigo(codigo: String, onEncontrada: (SolicitudEntity) -> Unit, onNoEncontrada: () -> Unit) {
        if (_isProcessingQr.value) return

        viewModelScope.launch {
            _isProcessingQr.value = true
            val encontrada = repository.obtenerPorCodigo(codigo)
            if (encontrada != null) {
                seleccionarSolicitud(encontrada)
                onEncontrada(encontrada)
            } else {
                onNoEncontrada()
            }
            _isProcessingQr.value = false
        }
    }

    fun prepararEdicion(solicitud: SolicitudEntity) {
        idEdicion = solicitud.id
        _isEditing.value = true
        _uiState.update {
            it.copy(
                titulo = solicitud.titulo,
                descripcion = solicitud.descripcion,
                categoria = solicitud.categoria,
                servicio = solicitud.servicio,
                evidenciaUri = solicitud.evidenciaUri,
                tituloError = null,
                descripcionError = null,
                categoriaError = null,
                servicioError = null
            )
        }
    }

    fun onTituloChanged(titulo: String) {
        _uiState.update {
            it.copy(titulo = titulo, tituloError = if (titulo.isBlank()) "El título es obligatorio" else null)
        }
    }

    fun onDescripcionChanged(descripcion: String) {
        val error = when {
            descripcion.isBlank() -> "La descripción es obligatoria"
            descripcion.length < 10 -> "Debe tener al menos 10 caracteres"
            else -> null
        }
        _uiState.update { it.copy(descripcion = descripcion, descripcionError = error) }
    }

    fun onCategoriaChanged(categoria: String) {
        _uiState.update {
            it.copy(categoria = categoria, categoriaError = if (categoria.isBlank()) "La categoría es obligatoria" else null)
        }
    }

    fun onServicioChanged(servicio: String) {
        _uiState.update {
            it.copy(servicio = servicio, servicioError = if (servicio.isBlank()) "El servicio relacionado es obligatorio" else null)
        }
    }

    // Al elegir un servicio del catálogo también se completa su categoría
    fun onServicioCatalogoElegido(servicio: String, categoria: String) {
        _uiState.update {
            it.copy(servicio = servicio, servicioError = null, categoria = categoria, categoriaError = null)
        }
    }

    fun onEvidenciaUriChanged(uri: String?) {
        _uiState.update { it.copy(evidenciaUri = uri) }
    }

    // Devuelve la solicitud creada (para mostrar su código único) o null si fue una edición
    fun validarYGuardar(onSuccess: (SolicitudEntity?) -> Unit) {
        val current = _uiState.value
        val tituloErr = if (current.titulo.isBlank()) "El título es obligatorio" else null
        val descErr = when {
            current.descripcion.isBlank() -> "La descripción es obligatoria"
            current.descripcion.length < 10 -> "Debe tener al menos 10 caracteres"
            else -> null
        }
        val catErr = if (current.categoria.isBlank()) "La categoría es obligatoria" else null
        val servErr = if (current.servicio.isBlank()) "El servicio relacionado es obligatorio" else null

        if (tituloErr != null || descErr != null || catErr != null || servErr != null) {
            _uiState.update {
                it.copy(tituloError = tituloErr, descripcionError = descErr, categoriaError = catErr, servicioError = servErr)
            }
            return
        }

        val actor = _usuario.value ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val ahora = ahora()
            var creada: SolicitudEntity? = null

            if (idEdicion == null) {
                val codigoUnico = "SOL-${System.currentTimeMillis().toString().takeLast(6)}"
                val nueva = SolicitudEntity(
                    codigo = codigoUnico,
                    titulo = current.titulo.trim(),
                    descripcion = current.descripcion.trim(),
                    categoria = current.categoria,
                    servicio = current.servicio,
                    evidenciaUri = current.evidenciaUri,
                    estado = Estados.RECIBIDA,
                    fechaHora = ahora,
                    clienteUsuario = actor.usuario,
                    clienteNombre = actor.nombre
                )
                val id = repository.insertar(nueva)
                creada = nueva.copy(id = id)
                repository.insertarHistorial(HistorialEntity(solicitudId = id, fechaHora = ahora, autor = actor.nombre, descripcion = "Solicitud creada (estado: ${Estados.RECIBIDA})"))
                repository.insertarAviso(AvisoEntity(destinatario = "rol:${Roles.ADMIN}", mensaje = "Nueva solicitud recibida: $codigoUnico - ${nueva.titulo}", fechaHora = ahora))
            } else {
                val existente = repository.obtenerPorId(idEdicion!!)
                if (existente != null) {
                    repository.actualizar(
                        existente.copy(
                            titulo = current.titulo.trim(),
                            descripcion = current.descripcion.trim(),
                            categoria = current.categoria,
                            servicio = current.servicio,
                            evidenciaUri = current.evidenciaUri,
                            fechaEdicion = ahora
                        )
                    )
                    repository.insertarHistorial(HistorialEntity(solicitudId = existente.id, fechaHora = ahora, autor = actor.nombre, descripcion = "Solicitud editada por el cliente"))
                }
            }

            resetForm()
            onSuccess(creada)
        }
    }

    fun resetForm() {
        idEdicion = null
        _isEditing.value = false
        _uiState.value = SolicitudUiState()
    }

    fun clearQrProcessing() {
        _isProcessingQr.value = false
    }

    // ---------- Flujo de estados ----------
    private fun ahora() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    // Cambia la solicitud, deja el registro en el historial y genera los avisos correspondientes
    private suspend fun aplicarCambio(
        id: Long,
        nuevoEstado: String?,
        detalle: String,
        avisos: List<Pair<String, String>> = emptyList(),
        modificar: (SolicitudEntity) -> SolicitudEntity = { it }
    ) {
        val actor = _usuario.value ?: return
        val actual = repository.obtenerPorId(id) ?: return
        val ahora = ahora()

        var nueva = modificar(actual).copy(fechaEdicion = ahora)
        if (nuevoEstado != null) nueva = nueva.copy(estado = nuevoEstado)
        repository.actualizar(nueva)

        repository.insertarHistorial(HistorialEntity(solicitudId = id, fechaHora = ahora, autor = actor.nombre, descripcion = detalle))

        val lista = avisos.toMutableList()
        // Aviso de "actualización de estado" para el cliente, salvo que él mismo haya hecho el cambio
        if (nuevoEstado != null && nuevoEstado != actual.estado && actor.usuario != actual.clienteUsuario) {
            val mensaje = if (nuevoEstado == Estados.RESUELTA)
                "Solicitud ${actual.codigo} resuelta (pendiente de confirmación del cliente)"
            else "Actualización de estado: ${actual.codigo} ahora está \"$nuevoEstado\""
            lista.add(actual.clienteUsuario to mensaje)
        }
        lista.forEach { (dest, msg) ->
            repository.insertarAviso(AvisoEntity(destinatario = dest, mensaje = msg, fechaHora = ahora))
        }
    }

    private suspend fun agregarComentario(id: Long, texto: String, evidenciaUri: String?) {
        val actor = _usuario.value ?: return
        if (texto.isBlank() && evidenciaUri == null) return
        repository.insertarComentario(
            ComentarioEntity(solicitudId = id, fechaHora = ahora(), autor = actor.nombre, rol = actor.rol, texto = texto.trim(), evidenciaUri = evidenciaUri)
        )
    }

    private fun accion(bloque: suspend () -> Unit) {
        viewModelScope.launch { bloque() }
    }

    // --- Administrador / Coordinador ---
    fun revisarYClasificar(id: Long, categoria: String) = accion {
        aplicarCambio(id, Estados.EN_REVISION, "Solicitud revisada y clasificada en la categoría \"$categoria\"") { it.copy(categoria = categoria) }
    }

    fun solicitarInformacion(id: Long, mensaje: String) = accion {
        agregarComentario(id, "Se solicita información adicional: $mensaje", null)
        aplicarCambio(id, Estados.PENDIENTE_INFO, "Se solicitó información adicional al cliente")
    }

    fun asignarTecnico(id: Long, prioridad: String, tecnico: UsuarioEntity) = accion {
        val codigo = repository.obtenerPorId(id)?.codigo ?: return@accion
        aplicarCambio(
            id, Estados.ASIGNADA,
            "Prioridad $prioridad y asignada al técnico ${tecnico.nombre}",
            avisos = listOf(tecnico.usuario to "Solicitud asignada: $codigo (prioridad $prioridad)")
        ) { it.copy(prioridad = prioridad, tecnicoUsuario = tecnico.usuario, tecnicoNombre = tecnico.nombre) }
    }

    fun cerrarSolicitud(id: Long) = accion {
        aplicarCambio(id, Estados.CERRADA, "Solicitud cerrada por el administrador")
    }

    fun cancelarSolicitud(id: Long, motivo: String) = accion {
        agregarComentario(id, "Cancelada: $motivo", null)
        aplicarCambio(id, Estados.CANCELADA, "Solicitud cancelada por el administrador")
    }

    // --- Técnico ---
    fun iniciarAtencion(id: Long) = accion {
        aplicarCambio(id, Estados.EN_PROCESO, "El técnico inició la atención")
    }

    fun registrarAvance(id: Long, texto: String, evidenciaUri: String?) = accion {
        agregarComentario(id, texto, evidenciaUri)
        aplicarCambio(id, null, "Avance registrado por el técnico")
    }

    fun proponerResolucion(id: Long, texto: String, evidenciaUri: String?) = accion {
        val codigo = repository.obtenerPorId(id)?.codigo ?: return@accion
        agregarComentario(id, "Propuesta de resolución: $texto", evidenciaUri)
        aplicarCambio(
            id, Estados.RESUELTA, "El técnico propuso la resolución",
            avisos = listOf("rol:${Roles.ADMIN}" to "Solicitud $codigo resuelta por el técnico")
        )
    }

    // --- Cliente ---
    fun responderInformacion(id: Long, texto: String, evidenciaUri: String?) = accion {
        val codigo = repository.obtenerPorId(id)?.codigo ?: return@accion
        agregarComentario(id, texto, evidenciaUri)
        aplicarCambio(
            id, Estados.EN_REVISION, "El cliente entregó la información solicitada",
            avisos = listOf("rol:${Roles.ADMIN}" to "El cliente respondió la solicitud $codigo")
        )
    }

    fun confirmarResolucion(id: Long) = accion {
        aplicarCambio(id, Estados.CERRADA, "El cliente confirmó la resolución")
    }

    fun pedirReapertura(id: Long, motivo: String) = accion {
        val codigo = repository.obtenerPorId(id)?.codigo ?: return@accion
        agregarComentario(id, "Reapertura solicitada: $motivo", null)
        aplicarCambio(
            id, Estados.EN_REVISION, "El cliente pidió reabrir la solicitud",
            avisos = listOf("rol:${Roles.ADMIN}" to "Reapertura solicitada para $codigo")
        )
    }
}
