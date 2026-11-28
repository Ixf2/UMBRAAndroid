
package com.example.umbra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FormularioPersonajeViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = PersonajeRepository()
  private val recursos = application.resources

  private val _personaje = MutableStateFlow<Personaje?>(null)
  val personaje = _personaje.asStateFlow()

  private val _cargando = MutableStateFlow(false)
  val cargando = _cargando.asStateFlow()

  private val _guardando = MutableStateFlow(false)
  val guardando = _guardando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  private val _guardado = MutableStateFlow(false)
  val guardado = _guardado.asStateFlow()

  private var idCargado: Int? = null

  fun cargarPersonaje(id: Int) {

    if (idCargado == id || _cargando.value) return

    if (id == -1) return

    viewModelScope.launch {

      _cargando.value = true

      _mensaje.value = recursos.getString(
        R.string.detalle_cargando_personaje
      )

      try {

        val respuesta = repository.obtenerPersonaje(id)

        if (respuesta.isSuccessful) {

          _personaje.value = respuesta.body()

          if (_personaje.value != null) {

            idCargado = id
            _mensaje.value = ""

          } else {

            _mensaje.value = recursos.getString(
              R.string.detalle_no_encontrado
            )
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
              R.string.estado_error_http,
              respuesta.code()
            )
          }
        }

      } catch (e: CancellationException) {

        throw e

      } catch (e: Exception) {

        _mensaje.value = recursos.getString(
          R.string.estado_error_conexion,
          e.localizedMessage.orEmpty()
        )

      } finally {

        _cargando.value = false
      }
    }
  }

  fun guardarPersonaje(
    id: Int,
    personaje: PersonajeRequest
  ) {

    if (
      _guardando.value ||
      _guardado.value ||
      _cargando.value
    ) return

    if (id != -1 && idCargado != id) return

    viewModelScope.launch {

      _guardando.value = true

      _mensaje.value = if (id == -1) {

        recursos.getString(
          R.string.formulario_guardando_personaje
        )

      } else {

        recursos.getString(
          R.string.formulario_actualizando_personaje
        )
      }

      try {

        val respuesta = if (id == -1) {

          repository.crearPersonaje(personaje)

        } else {

          repository.actualizarPersonaje(
            id,
            personaje
          )
        }

        if (respuesta.isSuccessful) {

          _mensaje.value = recursos.getString(
            R.string.formulario_guardado_correctamente
          )

          _guardado.value = true

        } else {

          _mensaje.value = when (respuesta.code()) {

            400 -> recursos.getString(
              R.string.formulario_datos_incorrectos
            )

            401 -> recursos.getString(
              R.string.detalle_sesion_requerida
            )

            403 -> recursos.getString(
              R.string.formulario_sin_permisos
            )

            404 -> recursos.getString(
              R.string.detalle_no_encontrado
            )

            else -> recursos.getString(
              R.string.estado_error_http,
              respuesta.code()
            )
          }
        }

      } catch (e: CancellationException) {

        throw e

      } catch (e: Exception) {

        _mensaje.value = recursos.getString(
          R.string.estado_error_conexion,
          e.localizedMessage.orEmpty()
        )

      } finally {

        _guardando.value = false
      }
    }
  }
}
