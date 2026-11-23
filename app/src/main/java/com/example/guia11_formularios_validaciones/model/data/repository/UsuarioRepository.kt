package com.example.guia11_formularios_validaciones.model.data.repository

import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioDao
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioEntity

class UsuarioRepository(private val dao: UsuarioDao) {

    // Perfiles ficticios que se cargan la primera vez que se abre la app
    private val perfiles = listOf(
        UsuarioEntity("cliente", "cliente123", "Cliente Demo", Roles.CLIENTE),
        UsuarioEntity("cliente2", "cliente123", "María Soto", Roles.CLIENTE),
        UsuarioEntity("admin", "admin123", "Administrador", Roles.ADMIN),
        UsuarioEntity("tecnico", "tecnico123", "Técnico Pérez", Roles.TECNICO),
        UsuarioEntity("tecnico2", "tecnico123", "Técnico Rojas", Roles.TECNICO)
    )

    suspend fun asegurarPerfiles() {
        if (dao.contar() == 0) dao.insertar(perfiles)
    }

    suspend fun autenticar(usuario: String, contrasena: String): UsuarioEntity? {
        asegurarPerfiles()
        return dao.buscar(usuario.trim())?.takeIf { it.contrasena == contrasena }
    }
}
