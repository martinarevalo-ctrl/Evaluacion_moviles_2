package com.example.guia11_formularios_validaciones.model.data.repository

import com.example.guia11_formularios_validaciones.model.data.local.CatalogoDao
import com.example.guia11_formularios_validaciones.model.data.local.CatalogoEntity
import com.example.guia11_formularios_validaciones.model.data.remote.ApiClient
import com.example.guia11_formularios_validaciones.model.data.remote.CatalogoApi
import com.example.guia11_formularios_validaciones.model.data.remote.ServicioCatalogo
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class CatalogoRepository(
    private val api: CatalogoApi = ApiClient.catalogoApi,
    private val catalogoDao: CatalogoDao? = null
) {

    val catalogoLocal: Flow<List<ServicioCatalogo>> = catalogoDao?.observarCatalogo()?.map { lista ->
        lista.map { ServicioCatalogo(it.id, it.servicio, it.categoria, it.descripcion) }
    } ?: flowOf(emptyList())

    // Nombres de nodo aceptados en Firebase (incluyendo "posts" con title, body, userId)
    private val nodosCatalogo = listOf("posts", "servicios", "catalogo", "catálogo", "catalogo_servicios", "servicios_informaticos")

    // Operación GET: Obtiene todos los objetos desde la API de Firebase y los guarda en el DAO local (Room)
    suspend fun obtenerServiciosDeApi(): Result<List<ServicioCatalogo>> = runCatching {
        val raiz = api.obtenerRaiz()
        val servicios = parsear(raiz)
        if (servicios.isEmpty()) error("El catálogo de Firebase está vacío o no tiene la estructura esperada")

        // Guardar en el DAO local (Room)
        catalogoDao?.let { dao ->
            dao.limpiarCatalogo()
            dao.insertarTodos(
                servicios.map {
                    CatalogoEntity(
                        id = it.id,
                        servicio = it.servicio,
                        categoria = it.categoria,
                        descripcion = it.descripcion
                    )
                }
            )
        }
        servicios
    }

    suspend fun obtenerServiciosLocales(): List<ServicioCatalogo> {
        val local = catalogoDao?.obtenerCatalogoList() ?: emptyList()
        if (local.isNotEmpty()) {
            return local.map { ServicioCatalogo(it.id, it.servicio, it.categoria, it.descripcion) }
        }
        return respaldo
    }

    // Admite arreglos [ {...} ] o mapas { clave: {...} } tanto para "posts" (title, body, userId) como para "servicios"
    internal fun parsear(raiz: JsonElement): List<ServicioCatalogo> {
        if (raiz.isJsonNull) return emptyList()

        val nodo: JsonElement = if (raiz is JsonObject) {
            val conocido = nodosCatalogo.firstNotNullOfOrNull { nombre ->
                raiz.entrySet().firstOrNull { it.key.equals(nombre, ignoreCase = true) }?.value
            }
            conocido ?: raiz.entrySet().map { it.value }.firstOrNull { contieneObjetos(it) } ?: raiz
        } else raiz

        val filas: List<Pair<String, JsonElement>> = when (nodo) {
            is JsonArray -> nodo.mapIndexed { i, e -> i.toString() to e }
            is JsonObject -> nodo.entrySet().map { it.key to it.value }
            else -> emptyList()
        }

        return filas.mapNotNull { (clave, valor) ->
            if (valor !is JsonObject) return@mapNotNull null
            // Soporta tanto "title" / "body" / "userId" (posts) como "servicio" / "descripcion" / "categoria"
            val nombre = texto(valor, "title", "servicio", "nombre", "name", "titulo", "título") ?: return@mapNotNull null
            val categoriaVal = texto(valor, "userId", "categoria", "categoría", "category")?.let {
                if (it.all { c -> c.isDigit() }) "Usuario $it" else it
            } ?: "General"

            ServicioCatalogo(
                id = texto(valor, "id", "ID", "codigo") ?: clave,
                servicio = nombre,
                categoria = categoriaVal,
                descripcion = texto(valor, "body", "descripcion", "descripción", "description") ?: ""
            )
        }
    }

    private fun contieneObjetos(e: JsonElement): Boolean = when (e) {
        is JsonArray -> e.any { it is JsonObject }
        is JsonObject -> e.entrySet().any { it.value is JsonObject }
        else -> false
    }

    // Busca una clave sin distinguir mayúsculas y devuelve su valor como texto
    private fun texto(obj: JsonObject, vararg claves: String): String? {
        for (clave in claves) {
            val entrada = obj.entrySet().firstOrNull { it.key.equals(clave, ignoreCase = true) } ?: continue
            val valor = entrada.value
            if (valor.isJsonPrimitive) return valor.asString.trim().ifEmpty { null }
        }
        return null
    }

    companion object {
        val respaldo = listOf(
            ServicioCatalogo("1", "Mantenimiento web", "Web", "Actualización y soporte de sitios web"),
            ServicioCatalogo("2", "Soporte de correo", "Soporte", "Configuración y solución de problemas"),
            ServicioCatalogo("3", "Hosting", "Infraestructura", "Alojamiento y gestión de sitios")
        )
    }
}
