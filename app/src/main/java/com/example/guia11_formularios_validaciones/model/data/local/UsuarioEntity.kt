package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Perfiles ficticios de la app (usuario y contraseña de ejemplo, sin seguridad real)
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val usuario: String,
    val contrasena: String,
    val nombre: String,
    val rol: String
)
