package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogo_servicios")
data class CatalogoEntity(
    @PrimaryKey
    val id: String,
    val servicio: String,
    val categoria: String,
    val descripcion: String
)
