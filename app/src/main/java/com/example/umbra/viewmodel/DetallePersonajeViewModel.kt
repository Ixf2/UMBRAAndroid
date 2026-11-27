
package com.example.umbra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.models.Personaje
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetallePersonajeViewModel(
  private val repository: PersonajeRepository = PersonajeRepository()
) : ViewModel() {

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

  suspend fun eliminarPersonaje(id: Int): Boolean {
    if (_eliminando.value) return false

    _eliminando.value = true
    _mensaje.value = "Eliminando personaje..."

    return try {
      val respuesta = repository.eliminarPersonaje(id)

      if (respuesta.isSuccessful) {
        true
      } else {
        _mensaje.value = when (respuesta.code()) {
          401 -> "Debes iniciar sesión"
          403 -> "No tienes permiso para eliminar"
          404 -> "Personaje no encontrado"
          else -> "Error HTTP: ${respuesta.code()}"
        }
        false
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      _mensaje.value =
        "Error de conexión: ${e.localizedMessage}"
      false
    } finally {
      _eliminando.value = false
    }
  }
}
