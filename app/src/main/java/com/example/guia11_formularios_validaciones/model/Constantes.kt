package com.example.guia11_formularios_validaciones.model

object Roles {
    const val CLIENTE = "cliente"
    const val ADMIN = "admin"
    const val TECNICO = "tecnico"

    fun etiqueta(rol: String) = when (rol) {
        ADMIN -> "Administrador / Coordinador"
        TECNICO -> "Técnico"
        else -> "Cliente"
    }
}

object Estados {
    const val RECIBIDA = "Recibida"
    const val EN_REVISION = "En revisión"
    const val PENDIENTE_INFO = "Pendiente de información"
    const val ASIGNADA = "Asignada"
    const val EN_PROCESO = "En proceso"
    const val RESUELTA = "Resuelta"
    const val CERRADA = "Cerrada"
    const val CANCELADA = "Cancelada"

    val todos = listOf(RECIBIDA, EN_REVISION, PENDIENTE_INFO, ASIGNADA, EN_PROCESO, RESUELTA, CERRADA, CANCELADA)

    // Estados en los que la solicitud ya no admite más cambios
    fun finalizado(estado: String) = estado == CERRADA || estado == CANCELADA
}

object Prioridades {
    const val SIN_DEFINIR = "Sin definir"
    val asignables = listOf("Baja", "Media", "Alta", "Crítica")
    val todas = listOf(SIN_DEFINIR) + asignables
}
