
package com.example.umbra.network

import com.example.umbra.models.LoginRequest
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.models.Usuario
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body credenciales: LoginRequest
    ): Response<Usuario>

    @GET("api/auth/me")
    suspend fun obtenerSesion(): Response<Usuario>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/personajes/publicos")
    suspend fun obtenerPublicos(): Response<List<Personaje>>

    @GET("api/personajes/mios")
    suspend fun obtenerMisPersonajes(): Response<List<Personaje>>

    @GET("api/personajes/{id}")
    suspend fun obtenerPersonaje(
        @Path("id") id: Int
    ): Response<Personaje>

    @POST("api/personajes")
    suspend fun crearPersonaje(
        @Body personaje: PersonajeRequest
    ): Response<Personaje>

    @PUT("api/personajes/{id}")
    suspend fun actualizarPersonaje(
        @Path("id") id: Int,
        @Body personaje: PersonajeRequest
    ): Response<Personaje>

    @DELETE("api/personajes/{id}")
    suspend fun eliminarPersonaje(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/personajes/{id}/imagen/miniatura")
    suspend fun obtenerMiniatura(
        @Path("id") id: Int
    ): Response<ResponseBody>

    @GET("api/personajes/{id}/imagen/detalle")
    suspend fun obtenerImagenDetalle(
        @Path("id") id: Int
    ): Response<ResponseBody>
}
