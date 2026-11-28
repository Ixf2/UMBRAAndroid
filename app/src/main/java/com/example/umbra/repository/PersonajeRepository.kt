
package com.example.umbra.repository

import okhttp3.MultipartBody
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.network.ApiService
import com.example.umbra.network.RetrofitClient
import retrofit2.Response

class PersonajeRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    suspend fun obtenerPublicos(): Response<List<Personaje>> {
        return api.obtenerPublicos()
    }

    suspend fun obtenerMisPersonajes(): Response<List<Personaje>> {
        return api.obtenerMisPersonajes()
    }

    suspend fun obtenerPersonaje(id: Int): Response<Personaje> {
        return api.obtenerPersonaje(id)
    }

    suspend fun crearPersonaje(
        personaje: PersonajeRequest
    ): Response<Personaje> {
        return api.crearPersonaje(personaje)
    }

    suspend fun actualizarPersonaje(
        id: Int,
        personaje: PersonajeRequest
    ): Response<Personaje> {
        return api.actualizarPersonaje(id, personaje)
    }

    suspend fun eliminarPersonaje(id: Int): Response<Unit> {
        return api.eliminarPersonaje(id)
    }

    suspend fun subirImagen(
        id: Int,
        archivo: MultipartBody.Part
    ): Response<Unit> {
        return api.subirImagen(id, archivo)
    }
}
