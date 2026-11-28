
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
import okhttp3.MultipartBody

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

  private val _subiendoImagen = MutableStateFlow(false)
  val subiendoImagen = _subiendoImagen.asStateFlow()

  private val _datosGuardados = MutableStateFlow(false)
  val datosGuardados = _datosGuardados.asStateFlow()

  private var idCargado: Int? = null
  private var idGuardado: Int? = null

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

          _mensaje.value = mensajeError(
            respuesta.code()
          )
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
    personaje: PersonajeRequest,
    archivo: MultipartBody.Part? = null
  ) {

    if (
      _guardando.value ||
      _guardado.value ||
      _cargando.value
    ) return

    if (id != -1 && idCargado != id) return

    if (_datosGuardados.value && idGuardado == null) return

    viewModelScope.launch {

      _guardando.value = true

      try {

        if (idGuardado == null) {

          _mensaje.value = recursos.getString(
            if (id == -1) {
              R.string.formulario_guardando_personaje
            } else {
              R.string.formulario_actualizando_personaje
            }
          )

          val respuesta = if (id == -1) {

            repository.crearPersonaje(personaje)

          } else {

            repository.actualizarPersonaje(
              id,
              personaje
            )
          }

          if (!respuesta.isSuccessful) {

            _mensaje.value = mensajeError(
              respuesta.code()
            )

            return@launch
          }

          _datosGuardados.value = true

          idGuardado = if (id == -1) {

            respuesta.body()?.idPersonaje

          } else {

            id
          }
        }

        val idPersonaje = idGuardado

        if (idPersonaje == null || idPersonaje <= 0) {

          _mensaje.value = recursos.getString(
            R.string.formulario_error_id
          )

          return@launch
        }

        if (archivo != null) {

          _subiendoImagen.value = true

          _mensaje.value = recursos.getString(
            R.string.imagen_subiendo
          )

          val respuestaImagen = repository.subirImagen(
            idPersonaje,
            archivo
          )

          if (!respuestaImagen.isSuccessful) {

            respuestaImagen.errorBody()?.close()

            val error = when (respuestaImagen.code()) {

              400 -> recursos.getString(
                R.string.imagen_error_procesamiento
              )

              413 -> recursos.getString(
                R.string.imagen_error_tamano
              )

              422 -> recursos.getString(
                R.string.imagen_error_optimizacion
              )

              else -> mensajeError(
                respuestaImagen.code()
              )
            }

            _mensaje.value = recursos.getString(
              R.string.imagen_error_subida
            ) + "\n" + error

            return@launch
          }
        }

        _mensaje.value = recursos.getString(
          R.string.formulario_guardado_correctamente
        )

        _guardado.value = true

      } catch (e: CancellationException) {

        throw e

      } catch (e: Exception) {

        val error = recursos.getString(
          R.string.estado_error_conexion,
          e.localizedMessage.orEmpty()
        )

        _mensaje.value = if (_datosGuardados.value) {

          recursos.getString(
            R.string.imagen_error_subida
          ) + "\n" + error

        } else {

          error
        }

      } finally {

        _subiendoImagen.value = false
        _guardando.value = false
      }
    }
  }

  private fun mensajeError(codigo: Int): String {

    return when (codigo) {

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
        codigo
      )
    }
  }
}
