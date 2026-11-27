
package com.example.umbra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.models.Personaje
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonajeViewModel(
  private val repository: PersonajeRepository = PersonajeRepository()
) : ViewModel() {

  private val _personajes =
    MutableStateFlow<List<Personaje>>(emptyList())

  val personajes = _personajes.asStateFlow()

  private val _cargando = MutableStateFlow(false)
  val cargando = _cargando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  fun cargarPersonajes(misPersonajes: Boolean) {
    viewModelScope.launch {
      _cargando.value = true
      _mensaje.value = "Cargando personajes..."

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
            "No hay personajes disponibles."
          } else {
            "Personajes encontrados: ${lista.size}"
          }
        } else {
          _personajes.value = emptyList()

          _mensaje.value = when (respuesta.code()) {
            401, 403 -> "Debes iniciar sesión"
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
}
