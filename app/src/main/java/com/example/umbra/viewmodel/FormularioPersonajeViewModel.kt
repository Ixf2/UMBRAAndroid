
package com.example.umbra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FormularioPersonajeViewModel(
  private val repository: PersonajeRepository = PersonajeRepository()
) : ViewModel() {

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

    if (idCargado == id) return

    idCargado = id

    if (id == -1) return

    viewModelScope.launch {
      _cargando.value = true
      _mensaje.value = "Cargando personaje..."

      try {
        val respuesta = repository.obtenerPersonaje(id)

        if (respuesta.isSuccessful) {
          _personaje.value = respuesta.body()

          _mensaje.value = if (_personaje.value == null) {
            "Personaje no encontrado"
          } else {
            ""
          }
        } else {
          _mensaje.value = when (respuesta.code()) {
            401 -> "Debes iniciar sesión"
            403 -> "No tienes permisos"
            404 -> "Personaje no encontrado"
            else -> "Error HTTP: ${respuesta.code()}"
          }
        }

      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _mensaje.value =
          "Error de conexión: ${e.localizedMessage}"
      } finally {
        _cargando.value = false
      }
    }
  }

  fun guardarPersonaje(
    id: Int,
    personaje: PersonajeRequest
  ) {

    if (_guardando.value || _guardado.value) return

    viewModelScope.launch {
      _guardando.value = true

      _mensaje.value = if (id == -1) {
        "Guardando personaje..."
      } else {
        "Actualizando personaje..."
      }

      try {
        val respuesta = if (id == -1) {
          repository.crearPersonaje(personaje)
        } else {
          repository.actualizarPersonaje(id, personaje)
        }

        if (respuesta.isSuccessful) {
          _mensaje.value = "Personaje guardado correctamente"
          _guardado.value = true
        } else {
          _mensaje.value = when (respuesta.code()) {
            400 -> "Revisa los datos del formulario"
            401 -> "Debes iniciar sesión"
            403 -> "No tienes permisos"
            404 -> "Personaje no encontrado"
            else -> "Error HTTP: ${respuesta.code()}"
          }
        }

      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _mensaje.value =
          "Error de conexión: ${e.localizedMessage}"
      } finally {
        _guardando.value = false
      }
    }
  }
}
