package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Historial de cambios de una solicitud (estado, prioridad, asignación...)
@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitudId: Long,
    val fechaHora: String,
    val autor: String,
    val descripcion: String
)

// Comentarios y evidencias (fotos / documentos) agregados durante el seguimiento
@Entity(tableName = "comentarios")
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitudId: Long,
    val fechaHora: String,
    val autor: String,
    val rol: String,
    val texto: String,
    val evidenciaUri: String? = null
)

// Avisos locales. destinatario es un usuario ("cliente") o un rol completo ("rol:admin")
@Entity(tableName = "avisos")
data class AvisoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinatario: String,
    val mensaje: String,
    val fechaHora: String,
    val leido: Boolean = false
)
