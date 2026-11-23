package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    val evidenciaUri: String? = null,
    val estado: String = "Recibida",
    val fechaHora: String,
    val fechaEdicion: String? = null,
    val clienteUsuario: String = "",
    val clienteNombre: String = "",
    val prioridad: String = "Sin definir",
    val tecnicoUsuario: String? = null,
    val tecnicoNombre: String? = null
)
