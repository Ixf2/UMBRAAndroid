
package com.example.umbra.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.adapters.PersonajeAdapter
import com.example.umbra.network.RetrofitClient
import com.example.umbra.utils.SessionManager
import com.example.umbra.viewmodel.PersonajeViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class BibliotecaActivity : AppCompatActivity() {

  private val api = RetrofitClient.apiService

  private lateinit var viewModel: PersonajeViewModel
  private lateinit var btnCargar: Button
  private lateinit var btnCrear: Button
  private lateinit var btnCerrarSesion: Button
  private lateinit var txtEstado: TextView
  private lateinit var recycler: RecyclerView

  private var sesionValidada = false
  private var cerrandoSesion = false
  private var verificandoSesion = false

  private val crearPersonajeLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { resultado ->

    if (resultado.resultCode == RESULT_OK) {
      verificarSesion()
    }
  }

  private val adapter = PersonajeAdapter(
    onPersonajeClick = { personaje ->

      if (
        sesionValidada &&
        !cerrandoSesion &&
        !verificandoSesion
      ) {

        val intent = Intent(
          this,
          DetallePersonajeActivity::class.java
        )

        intent.putExtra(
          "idPersonaje",
          personaje.idPersonaje
        )

        intent.putExtra(
          "puedeEditar",
          true
        )

        crearPersonajeLauncher.launch(intent)
      }
    }
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_biblioteca_activity)

    viewModel = ViewModelProvider(this)[
      PersonajeViewModel::class.java
    ]

    btnCargar = findViewById(R.id.btnCargarPersonajes)
    btnCrear = findViewById(R.id.btnCrearPersonaje)
    btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    txtEstado = findViewById(R.id.txtEstado)
    recycler = findViewById(R.id.recyclerPersonajes)

    val titulo = findViewById<TextView>(
      R.id.tituloBiblioteca
    )

    titulo.text = getString(
      R.string.biblioteca_mis_personajes
    )

    btnCargar.text = getString(
      R.string.biblioteca_cargar_mios
    )

    btnCrear.visibility = View.VISIBLE
    btnCerrarSesion.visibility = View.VISIBLE
    recycler.visibility = View.INVISIBLE

    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter

    actualizarBotones()
    observarViewModel()

    btnCargar.setOnClickListener {
      verificarSesion()
    }

    btnCrear.setOnClickListener {

      if (
        sesionValidada &&
        !cerrandoSesion &&
        !verificandoSesion
      ) {

        val intent = Intent(
          this,
          PersonajeActivity::class.java
        )

        crearPersonajeLauncher.launch(intent)
      }
    }

    btnCerrarSesion.setOnClickListener {
      cerrarSesion()
    }
  }

  override fun onStart() {
    super.onStart()
    verificarSesion()
  }

  private fun verificarSesion() {

    if (verificandoSesion || cerrandoSesion) return

    lifecycleScope.launch {

      verificandoSesion = true
      sesionValidada = false

      recycler.visibility = View.INVISIBLE

      actualizarBotones()

      txtEstado.text = getString(
        R.string.estado_verificando_sesion
      )

      try {

        when (SessionManager.verificarSesion()) {

          SessionManager.EstadoSesion.AUTENTICADA -> {

            sesionValidada = true

            recycler.visibility = View.VISIBLE

            cargarPersonajes()
          }

          SessionManager.EstadoSesion.NO_AUTENTICADA -> {

            adapter.actualizarPersonajes(emptyList())

            SessionManager.volverLogin(
              this@BibliotecaActivity
            )
          }

          SessionManager.EstadoSesion.ERROR_CONEXION -> {

            txtEstado.text = getString(
              R.string.sesion_error_conexion
            )
          }

          SessionManager.EstadoSesion.ERROR_SERVIDOR -> {

            txtEstado.text = getString(
              R.string.sesion_error_servidor
            )
          }
        }

      } finally {

        verificandoSesion = false

        actualizarBotones()
      }
    }
  }

  private fun observarViewModel() {

    lifecycleScope.launch {

      repeatOnLifecycle(Lifecycle.State.STARTED) {

        launch {

          viewModel.personajes.collect { personajes ->

            if (sesionValidada && !cerrandoSesion) {

              adapter.actualizarPersonajes(personajes)
            }
          }
        }

        launch {

          viewModel.mensaje.collect { mensaje ->

            if (sesionValidada && !cerrandoSesion) {

              txtEstado.text = mensaje
            }
          }
        }

        launch {

          viewModel.cargando.collect {

            actualizarBotones()
          }
        }
      }
    }
  }

  private fun actualizarBotones() {

    val disponible =
      sesionValidada &&
          !cerrandoSesion &&
          !verificandoSesion &&
          !viewModel.cargando.value

    btnCargar.isEnabled =
      !cerrandoSesion &&
          !verificandoSesion &&
          !viewModel.cargando.value

    btnCrear.isEnabled = disponible

    btnCerrarSesion.isEnabled =
      !cerrandoSesion && !verificandoSesion
  }

  private fun cargarPersonajes() {

    if (sesionValidada && !cerrandoSesion) {

      viewModel.cargarPersonajes(true)
    }
  }

  private fun cerrarSesion() {

    if (cerrandoSesion || verificandoSesion) return

    lifecycleScope.launch {

      cerrandoSesion = true

      actualizarBotones()

      txtEstado.text = getString(
        R.string.estado_cerrando_sesion
      )

      try {

        val respuesta = api.logout()

        if (
          respuesta.isSuccessful ||
          respuesta.code() == 401
        ) {

          sesionValidada = false

          recycler.visibility = View.INVISIBLE

          adapter.actualizarPersonajes(emptyList())

          SessionManager.volverLogin(
            this@BibliotecaActivity
          )

        } else {

          respuesta.errorBody()?.close()

          txtEstado.text = getString(
            R.string.estado_error_cerrar_sesion,
            respuesta.code()
          )
        }

      } catch (e: CancellationException) {

        throw e

      } catch (e: Exception) {

        txtEstado.text = getString(
          R.string.estado_error_conexion,
          e.localizedMessage.orEmpty()
        )

      } finally {

        cerrandoSesion = false

        actualizarBotones()
      }
    }
  }
}
