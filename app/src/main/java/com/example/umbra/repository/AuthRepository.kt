package com.example.umbra.repository

import com.example.umbra.models.LoginRequest
import com.example.umbra.models.RegistroRequest
import com.example.umbra.models.Usuario
import com.example.umbra.network.RetrofitClient
import retrofit2.Response

class AuthRepository {

  private val api = RetrofitClient.apiService

  suspend fun registrar(
    datos: RegistroRequest
  ): Response<Usuario> {
    return api.registrar(datos)
  }

  suspend fun login(
    credenciales: LoginRequest
  ): Response<Usuario> {
    return api.login(credenciales)
  }

  suspend fun obtenerSesion(): Response<Usuario> {
    return api.obtenerSesion()
  }

  suspend fun cerrarSesion(): Response<Unit> {
    return api.logout()
  }
}