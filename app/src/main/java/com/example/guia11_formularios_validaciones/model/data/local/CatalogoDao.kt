package com.example.guia11_formularios_validaciones.model.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {
    @Query("SELECT * FROM catalogo_servicios")
    fun observarCatalogo(): Flow<List<CatalogoEntity>>

    @Query("SELECT * FROM catalogo_servicios")
    suspend fun obtenerCatalogoList(): List<CatalogoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(servicios: List<CatalogoEntity>)

    @Query("DELETE FROM catalogo_servicios")
    suspend fun limpiarCatalogo()
}
