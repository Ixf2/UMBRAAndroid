
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

class PersonajeViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = PersonajeRepository()
  private val recursos = application.resources

  private val _personajes =
    MutableStateFlow<List<Personaje>>(emptyList())

  val personajes = _personajes.asStateFlow()

  private val _cargando = MutableStateFlow(false)
  val cargando = _cargando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  fun cargarPersonajes(misPersonajes: Boolean) {

    if (_cargando.value) return

    viewModelScope.launch {

      _cargando.value = true

      _mensaje.value = recursos.getString(
        R.string.estado_cargando_personajes
      )

      try {

        val respuesta = if (misPersonajes) {
          repository.obtenerMisPersonajes()
        } else {
          repository.obtenerPublicos()
        }

        if (respuesta.isSuccessful) {

          val lista = respuesta.body().orEmpty()

          _personajes.value = lista

          _mensaje.value = if (lista.isEmpty()) {

            recursos.getString(
              R.string.biblioteca_sin_personajes
            )

          } else {

            recursos.getQuantityString(
              R.plurals.biblioteca_personajes_encontrados,
              lista.size,
              lista.size
            )
          }

        } else {

          _personajes.value = emptyList()

          _mensaje.value = when (respuesta.code()) {

            401, 403 -> recursos.getString(
              R.string.detalle_sesion_requerida
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
}
