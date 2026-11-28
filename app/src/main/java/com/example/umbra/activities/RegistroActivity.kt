
package com.example.umbra.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.umbra.R
import com.example.umbra.viewmodel.RegistroViewModel
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

  private lateinit var viewModel: RegistroViewModel

  private lateinit var etNombre: EditText
  private lateinit var etEmail: EditText
  private lateinit var etPassword: EditText
  private lateinit var etConfirmarPassword: EditText

  private lateinit var txtEstado: TextView
  private lateinit var progreso: ProgressBar

  private lateinit var btnRegistrar: Button
  private lateinit var btnVolverLogin: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_registro)

    viewModel = ViewModelProvider(this)[
      RegistroViewModel::class.java
    ]

    etNombre = findViewById(R.id.etNombreRegistro)
    etEmail = findViewById(R.id.etEmailRegistro)
    etPassword = findViewById(R.id.etPasswordRegistro)
    etConfirmarPassword = findViewById(R.id.etConfirmarPassword)

    txtEstado = findViewById(R.id.txtEstadoRegistro)
    progreso = findViewById(R.id.progresoRegistro)

    btnRegistrar = findViewById(R.id.btnRegistrar)
    btnVolverLogin = findViewById(R.id.btnVolverLogin)

    btnRegistrar.setOnClickListener {
      registrarUsuario()
    }

    btnVolverLogin.setOnClickListener {
      if (!viewModel.registrando.value) {
        finish()
      }
    }

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
          if (!viewModel.registrando.value) {
            finish()
          }
        }
      }
    )

    actualizarFormulario(viewModel.registrando.value)

    observarViewModel()
  }

  private fun registrarUsuario() {

    if (viewModel.registrando.value) return

    viewModel.registrar(
      nombreUsuario = etNombre.text.toString(),
      email = etEmail.text.toString(),
      password = etPassword.text.toString(),
      confirmarPassword = etConfirmarPassword.text.toString()
    )
  }

  private fun observarViewModel() {

    lifecycleScope.launch {

      repeatOnLifecycle(Lifecycle.State.STARTED) {

        launch {

          viewModel.registrando.collect { registrando ->

            actualizarFormulario(registrando)
          }
        }

        launch {

          viewModel.mensaje.collect { mensaje ->

            txtEstado.text = mensaje
          }
        }

        launch {

          viewModel.registrado.collect { registrado ->

            if (registrado && !isFinishing) {

              etPassword.text.clear()
              etConfirmarPassword.text.clear()

              Toast.makeText(
                this@RegistroActivity,
                R.string.registro_correcto,
                Toast.LENGTH_LONG
              ).show()

              setResult(RESULT_OK)
              finish()
            }
          }
        }
      }
    }
  }

  private fun actualizarFormulario(registrando: Boolean) {

    val habilitado =
      !registrando && !viewModel.registrado.value

    etNombre.isEnabled = habilitado
    etEmail.isEnabled = habilitado
    etPassword.isEnabled = habilitado
    etConfirmarPassword.isEnabled = habilitado

    btnRegistrar.isEnabled = habilitado
    btnVolverLogin.isEnabled = habilitado

    progreso.visibility = if (registrando) {
      View.VISIBLE
    } else {
      View.GONE
    }
  }
}
