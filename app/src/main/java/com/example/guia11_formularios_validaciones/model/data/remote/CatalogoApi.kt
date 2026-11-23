package com.example.guia11_formularios_validaciones.model.data.remote

import com.google.gson.JsonElement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Catálogo externo de servicios informáticos: Firebase Realtime Database (API REST).
// Reemplaza a la API de prueba (jsonplaceholder) que se usaba como servicio externo.
const val FIREBASE_URL = "https://solicitud-2d314-default-rtdb.firebaseio.com/"

interface CatalogoApi {
    // Se lee la raíz de la base y se busca el nodo del catálogo (ver CatalogoRepository)
    @GET(".json")
    suspend fun obtenerRaiz(): JsonElement
}

object ApiClient {
    val catalogoApi: CatalogoApi by lazy {
        Retrofit.Builder()
            .baseUrl(FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatalogoApi::class.java)
    }
}
