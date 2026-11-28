
package com.example.umbra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetallePersonajeViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = PersonajeRepository()
  private val recursos = application.resources

  private val _personaje = MutableStateFlow<Personaje?>(null)
  val personaje = _personaje.asStateFlow()

  private val _cargando = MutableStateFlow(false)
  val cargando = _cargando.asStateFlow()

  private val _eliminando = MutableStateFlow(false)
  val eliminando = _eliminando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  fun cargarPersonaje(id: Int) {

    viewModelScope.launch {

      _cargando.value = true
      _personaje.value = null

      _mensaje.value = recursos.getString(
        R.string.detalle_cargando_personaje
      )

      try {

        val respuesta = repository.obtenerPersonaje(id)

        if (respuesta.isSuccessful) {

          _personaje.value = respuesta.body()

          _mensaje.value = if (_personaje.value == null) {

            recursos.getString(
              R.string.detalle_no_encontrado
            )

          } else {
            ""
          }

        } else {

          _mensaje.value = when (respuesta.code()) {

            401 -> recursos.getString(
              R.string.detalle_sesion_requerida
            )

            403 -> recursos.getString(
              R.string.detalle_sin_permisos
            )

            404 -> recursos.getString(
              R.string.detalle_no_encontrado
            )

            else -> recursos.getString(
              R.string.detalle_error_http,
              respuesta.code()
            )
          }
        }

      } catch (e: CancellationException) {

        throw e

      } catch (e: Exception) {

        _mensaje.value = recursos.getString(
          R.string.detalle_error_conexion,
          e.localizedMessage.orEmpty()
        )

      } finally {

        _cargando.value = false
      }
    }
  }

  suspend fun eliminarPersonaje(id: Int): Boolean {

    if (_eliminando.value) return false

    _eliminando.value = true

    _mensaje.value = recursos.getString(
      R.string.eliminar_procesando
    )

    return try {

      val respuesta = repository.eliminarPersonaje(id)

      if (respuesta.isSuccessful) {

        true

      } else {

        _mensaje.value = when (respuesta.code()) {

          401 -> recursos.getString(
            R.string.detalle_sesion_requerida
          )

          403 -> recursos.getString(
            R.string.eliminar_sin_permisos
          )

          404 -> recursos.getString(
            R.string.detalle_no_encontrado
          )

          else -> recursos.getString(
            R.string.detalle_error_http,
            respuesta.code()
          )
        }

        false
      }

    } catch (e: CancellationException) {

      throw e

    } catch (e: Exception) {

      _mensaje.value = recursos.getString(
        R.string.detalle_error_conexion,
        e.localizedMessage.orEmpty()
      )

      false

    } finally {

      _eliminando.value = false
    }
  }
}
