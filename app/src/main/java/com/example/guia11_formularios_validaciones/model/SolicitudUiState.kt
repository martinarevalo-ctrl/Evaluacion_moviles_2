package com.example.guia11_formularios_validaciones.model

import com.example.guia11_formularios_validaciones.model.data.remote.ServicioCatalogo

data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Soporte Técnico",
    val servicio: String = "",
    val evidenciaUri: String? = null,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)

// Estado de la carga del catálogo externo (Firebase)
data class CatalogoUiState(
    val servicios: List<ServicioCatalogo> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null
)

// Filtros de la lista de solicitudes (vacío = sin filtrar)
data class FiltrosUiState(
    val cliente: String = "",
    val estado: String = "",
    val prioridad: String = "",
    val responsable: String = ""
)

// Datos del panel resumen del administrador
data class ResumenUi(
    val total: Int = 0,
    val sinAsignar: Int = 0,
    val porEstado: Map<String, Int> = emptyMap(),
    val porPrioridad: Map<String, Int> = emptyMap(),
    val cargaTecnicos: Map<String, Int> = emptyMap()
)
