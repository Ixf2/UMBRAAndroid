
package com.example.umbra.utils

import android.app.Activity
import android.content.Intent
import com.example.umbra.activities.MainActivity
import com.example.umbra.network.RetrofitClient
import kotlinx.coroutines.CancellationException

object SessionManager {

  private val api = RetrofitClient.apiService

  enum class EstadoSesion {
    AUTENTICADA,
    NO_AUTENTICADA,
    ERROR_CONEXION,
    ERROR_SERVIDOR
  }

  suspend fun verificarSesion(): EstadoSesion {

    return try {

      val respuesta = api.obtenerSesion()

      val estado = when {

        respuesta.isSuccessful &&
            respuesta.body() != null -> {

          EstadoSesion.AUTENTICADA
        }

        respuesta.code() == 401 ||
            respuesta.code() == 403 -> {

          EstadoSesion.NO_AUTENTICADA
        }

        else -> {

          EstadoSesion.ERROR_SERVIDOR
        }
      }

      respuesta.errorBody()?.close()

      estado

    } catch (e: CancellationException) {

      throw e

    } catch (e: Exception) {

      EstadoSesion.ERROR_CONEXION
    }
  }

  fun volverLogin(activity: Activity) {

    val intent = Intent(
      activity,
      MainActivity::class.java
    )

    intent.flags =
      Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TASK

    activity.startActivity(intent)
    activity.finish()
  }
}
