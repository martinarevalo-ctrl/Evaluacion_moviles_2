package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun obtenerSolicitudes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudPorId(id: Long): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerSolicitudPorCodigo(codigo: String): SolicitudEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizarSolicitud(solicitud: SolicitudEntity)

    @Delete
    suspend fun eliminarSolicitud(solicitud: SolicitudEntity)
}

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscar(usuario: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuarios: List<UsuarioEntity>)
}

@Dao
interface SeguimientoDao {
    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity)

    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY id DESC")
    fun observarHistorial(solicitudId: Long): Flow<List<HistorialEntity>>

    @Insert
    suspend fun insertarComentario(comentario: ComentarioEntity)

    @Query("SELECT * FROM comentarios WHERE solicitudId = :solicitudId ORDER BY id ASC")
    fun observarComentarios(solicitudId: Long): Flow<List<ComentarioEntity>>

    @Insert
    suspend fun insertarAviso(aviso: AvisoEntity)

    @Query("SELECT * FROM avisos WHERE destinatario IN (:destinatarios) ORDER BY id DESC")
    fun observarAvisos(destinatarios: List<String>): Flow<List<AvisoEntity>>

    @Query("UPDATE avisos SET leido = 1 WHERE destinatario IN (:destinatarios)")
    suspend fun marcarAvisosLeidos(destinatarios: List<String>)
}
