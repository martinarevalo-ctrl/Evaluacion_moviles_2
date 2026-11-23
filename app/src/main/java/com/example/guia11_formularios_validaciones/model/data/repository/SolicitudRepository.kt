package com.example.guia11_formularios_validaciones.model.data.repository

import com.example.guia11_formularios_validaciones.model.Roles
import com.example.guia11_formularios_validaciones.model.data.local.AvisoEntity
import com.example.guia11_formularios_validaciones.model.data.local.ComentarioEntity
import com.example.guia11_formularios_validaciones.model.data.local.HistorialEntity
import com.example.guia11_formularios_validaciones.model.data.local.SeguimientoDao
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudDao
import com.example.guia11_formularios_validaciones.model.data.local.SolicitudEntity
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioDao
import com.example.guia11_formularios_validaciones.model.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(
    private val solicitudDao: SolicitudDao,
    private val usuarioDao: UsuarioDao,
    private val seguimientoDao: SeguimientoDao
) {
    val listaSolicitudes: Flow<List<SolicitudEntity>> = solicitudDao.obtenerSolicitudes()
    val tecnicos: Flow<List<UsuarioEntity>> = usuarioDao.observarPorRol(Roles.TECNICO)

    suspend fun insertar(solicitud: SolicitudEntity): Long = solicitudDao.insertarSolicitud(solicitud)

    suspend fun obtenerPorId(id: Long): SolicitudEntity? = solicitudDao.obtenerSolicitudPorId(id)

    suspend fun obtenerPorCodigo(codigo: String): SolicitudEntity? = solicitudDao.obtenerSolicitudPorCodigo(codigo)

    suspend fun actualizar(solicitud: SolicitudEntity) = solicitudDao.actualizarSolicitud(solicitud)

    suspend fun eliminar(solicitud: SolicitudEntity) = solicitudDao.eliminarSolicitud(solicitud)

    // Seguimiento
    fun observarHistorial(id: Long): Flow<List<HistorialEntity>> = seguimientoDao.observarHistorial(id)
    fun observarComentarios(id: Long): Flow<List<ComentarioEntity>> = seguimientoDao.observarComentarios(id)
    suspend fun insertarHistorial(h: HistorialEntity) = seguimientoDao.insertarHistorial(h)
    suspend fun insertarComentario(c: ComentarioEntity) = seguimientoDao.insertarComentario(c)

    // Avisos
    fun observarAvisos(destinatarios: List<String>): Flow<List<AvisoEntity>> = seguimientoDao.observarAvisos(destinatarios)
    suspend fun insertarAviso(a: AvisoEntity) = seguimientoDao.insertarAviso(a)
    suspend fun marcarAvisosLeidos(destinatarios: List<String>) = seguimientoDao.marcarAvisosLeidos(destinatarios)
}
