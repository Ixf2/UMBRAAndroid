package com.example.umbra.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetallePersonajeViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = PersonajeRepository()
  private val recursos = application.resources

  private val _personaje = MutableStateFlow<Personaje?>(null)
  val personaje = _personaje.asStateFlow()

  private val _imagenDetalle = MutableStateFlow<Bitmap?>(null)
  val imagenDetalle = _imagenDetalle.asStateFlow()

  private val _cargando = MutableStateFlow(false)
  val cargando = _cargando.asStateFlow()

  private val _eliminando = MutableStateFlow(false)
  val eliminando = _eliminando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  private var trabajoImagen: Job? = null

  fun cargarPersonaje(id: Int) {
    if (_cargando.value || _eliminando.value) return

    trabajoImagen?.cancel()
    _cargando.value = true
    _personaje.value = null
    _imagenDetalle.value = null
    _mensaje.value = recursos.getString(R.string.detalle_cargando_personaje)

    viewModelScope.launch {
      try {
        val respuesta = repository.obtenerPersonaje(id)

        if (respuesta.isSuccessful) {
          val datos = respuesta.body()
          _personaje.value = datos

          if (datos == null) {
            _mensaje.value = recursos.getString(R.string.detalle_no_encontrado)
          } else {
            _mensaje.value = ""
            cargarImagen(datos.idPersonaje)
          }
        } else {
          respuesta.errorBody()?.close()
          _mensaje.value = when (respuesta.code()) {
            401 -> recursos.getString(R.string.detalle_sesion_requerida)
            403 -> recursos.getString(R.string.detalle_sin_permisos)
            404 -> recursos.getString(R.string.detalle_no_encontrado)
            else -> recursos.getString(R.string.detalle_error_http, respuesta.code())
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

  private fun cargarImagen(id: Int) {
    trabajoImagen?.cancel()

    trabajoImagen = viewModelScope.launch {
      try {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
          val respuesta = repository.obtenerImagenDetalle(id)

          if (respuesta.isSuccessful) {
            respuesta.body()?.use { cuerpo ->
              BitmapFactory.decodeStream(cuerpo.byteStream())
            }
          } else {
            respuesta.errorBody()?.close()
            if (respuesta.code() != 404) {
              Log.w("UMBRA_IMAGEN", "Detalle HTTP: ${respuesta.code()}")
            }
            null
          }
        }

        if (_personaje.value?.idPersonaje == id) {
          _imagenDetalle.value = bitmap
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e("UMBRA_IMAGEN", "Error al cargar retrato de detalle", e)
      }
    }
  }

  suspend fun eliminarPersonaje(id: Int): Boolean {
    if (_eliminando.value || _cargando.value) return false

    _eliminando.value = true
    _mensaje.value = recursos.getString(R.string.eliminar_procesando)

    return try {
      val respuesta = repository.eliminarPersonaje(id)

      if (respuesta.isSuccessful) {
        trabajoImagen?.cancel()
        _personaje.value = null
        _imagenDetalle.value = null
        true
      } else {
        respuesta.errorBody()?.close()
        _mensaje.value = when (respuesta.code()) {
          401 -> recursos.getString(R.string.detalle_sesion_requerida)
          403 -> recursos.getString(R.string.eliminar_sin_permisos)
          404 -> recursos.getString(R.string.detalle_no_encontrado)
          else -> recursos.getString(R.string.detalle_error_http, respuesta.code())
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
