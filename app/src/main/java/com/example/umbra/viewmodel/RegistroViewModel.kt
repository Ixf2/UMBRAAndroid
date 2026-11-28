
package com.example.umbra.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbra.R
import com.example.umbra.models.RegistroRequest
import com.example.umbra.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = AuthRepository()
  private val recursos = application.resources

  private val _registrando = MutableStateFlow(false)
  val registrando = _registrando.asStateFlow()

  private val _mensaje = MutableStateFlow("")
  val mensaje = _mensaje.asStateFlow()

  private val _registrado = MutableStateFlow(false)
  val registrado = _registrado.asStateFlow()

  fun registrar(
    nombreUsuario: String,
    email: String,
    password: String,
    confirmarPassword: String
  ) {

    if (_registrando.value || _registrado.value) return

    val nombre = nombreUsuario.trim()
    val correo = email.trim()

    _mensaje.value = ""

    when {

      nombre.length !in 2..50 -> {
        _mensaje.value = recursos.getString(
          R.string.registro_nombre_invalido
        )
        return
      }

      correo.length > 100 ||
          !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {

        _mensaje.value = recursos.getString(
          R.string.registro_email_invalido
        )
        return
      }

      password.isBlank() ||
          password.length !in 8..72 ||
          password.toByteArray(Charsets.UTF_8).size > 72 -> {

        _mensaje.value = recursos.getString(
          R.string.registro_password_invalida
        )
        return
      }

      password != confirmarPassword -> {

        _mensaje.value = recursos.getString(
          R.string.registro_password_no_coincide
        )
        return
      }
    }

    viewModelScope.launch {

      _registrando.value = true

      _mensaje.value = recursos.getString(
        R.string.registro_procesando
      )

      try {

        val datos = RegistroRequest(
          nombreUsuario = nombre,
          email = correo,
          password = password
        )

        val respuesta = repository.registrar(datos)

        if (respuesta.isSuccessful) {

          _mensaje.value = recursos.getString(
            R.string.registro_correcto
          )

          _registrado.value = true

        } else {

          respuesta.errorBody()?.close()

          _mensaje.value = when (respuesta.code()) {

            400 -> recursos.getString(
              R.string.registro_datos_invalidos
            )

            409 -> recursos.getString(
              R.string.registro_email_duplicado
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

        _registrando.value = false
      }
    }
  }
}
