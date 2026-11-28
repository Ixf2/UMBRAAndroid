
package com.example.umbra.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
  private lateinit var btnMisPersonajes: Button
  private lateinit var btnPersonajesPublicos: Button
  private lateinit var txtEstado: TextView
  private lateinit var recycler: RecyclerView

  private var mostrarMisPersonajes = true
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
        !verificandoSesion &&
        !viewModel.cargando.value
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
          mostrarMisPersonajes
        )

        crearPersonajeLauncher.launch(intent)
      }
    }
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_biblioteca_activity)

    mostrarMisPersonajes = savedInstanceState?.getBoolean(
      "mostrarMisPersonajes",
      true
    ) ?: true

    viewModel = ViewModelProvider(this)[
      PersonajeViewModel::class.java
    ]

    btnCargar = findViewById(R.id.btnCargarPersonajes)
    btnCrear = findViewById(R.id.btnCrearPersonaje)
    btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    btnMisPersonajes = findViewById(R.id.btnMisPersonajes)
    btnPersonajesPublicos = findViewById(R.id.btnPersonajesPublicos)
    txtEstado = findViewById(R.id.txtEstado)
    recycler = findViewById(R.id.recyclerPersonajes)

    val titulo = findViewById<TextView>(
      R.id.tituloBiblioteca
    )

    titulo.text = getString(
      R.string.biblioteca_titulo_general
    )

    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter
    recycler.visibility = View.INVISIBLE

    btnCerrarSesion.visibility = View.VISIBLE

    actualizarPestanas()
    actualizarBotones()
    observarViewModel()

    btnMisPersonajes.setOnClickListener {
      cambiarBiblioteca(true)
    }

    btnPersonajesPublicos.setOnClickListener {
      cambiarBiblioteca(false)
    }

    btnCargar.setOnClickListener {
      verificarSesion()
    }

    btnCrear.setOnClickListener {

      if (
        sesionValidada &&
        mostrarMisPersonajes &&
        !cerrandoSesion &&
        !verificandoSesion &&
        !viewModel.cargando.value
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

  override fun onSaveInstanceState(outState: Bundle) {

    outState.putBoolean(
      "mostrarMisPersonajes",
      mostrarMisPersonajes
    )

    super.onSaveInstanceState(outState)
  }

  private fun cambiarBiblioteca(misPersonajes: Boolean) {

    if (
      !sesionValidada ||
      verificandoSesion ||
      cerrandoSesion ||
      viewModel.cargando.value ||
      mostrarMisPersonajes == misPersonajes
    ) return

    mostrarMisPersonajes = misPersonajes

    recycler.visibility = View.INVISIBLE

    adapter.actualizarPersonajes(emptyList())

    actualizarPestanas()
    verificarSesion()
  }

  private fun actualizarPestanas() {

    aplicarEstiloPestana(
      btnMisPersonajes,
      mostrarMisPersonajes
    )

    aplicarEstiloPestana(
      btnPersonajesPublicos,
      !mostrarMisPersonajes
    )

    btnCargar.text = getString(
      if (mostrarMisPersonajes) {
        R.string.biblioteca_cargar_mios
      } else {
        R.string.button_cargar_personajesPublicos
      }
    )

    btnCrear.visibility = if (mostrarMisPersonajes) {
      View.VISIBLE
    } else {
      View.GONE
    }
  }

  private fun aplicarEstiloPestana(
    boton: Button,
    seleccionada: Boolean
  ) {

    val fondo = ContextCompat.getColor(
      this,
      if (seleccionada) {
        R.color.umbra_primary
      } else {
        R.color.umbra_surface_variant
      }
    )

    val texto = ContextCompat.getColor(
      this,
      if (seleccionada) {
        R.color.umbra_on_primary
      } else {
        R.color.umbra_text
      }
    )

    boton.backgroundTintList =
      ColorStateList.valueOf(fondo)

    boton.setTextColor(texto)
    boton.isSelected = seleccionada
  }

  private fun verificarSesion() {

    if (verificandoSesion || cerrandoSesion || isFinishing) return

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
        actualizarListado()
      }
    }
  }

  private fun observarViewModel() {

    lifecycleScope.launch {

      repeatOnLifecycle(Lifecycle.State.STARTED) {

        launch {

          viewModel.personajes.collect {

            actualizarListado()
          }
        }

        launch {

          viewModel.mensaje.collect { mensaje ->

            if (
              sesionValidada &&
              !cerrandoSesion &&
              !verificandoSesion
            ) {

              txtEstado.text = mensaje
            }
          }
        }

        launch {

          viewModel.cargando.collect {

            actualizarBotones()
            actualizarListado()
          }
        }
      }
    }
  }

  private fun actualizarListado() {

    val disponible =
      sesionValidada &&
          !cerrandoSesion &&
          !verificandoSesion &&
          !viewModel.cargando.value &&
          !isFinishing

    if (disponible) {

      adapter.actualizarPersonajes(
        viewModel.personajes.value
      )

      recycler.visibility = View.VISIBLE

    } else {

      recycler.visibility = View.INVISIBLE
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

    btnMisPersonajes.isEnabled = disponible
    btnPersonajesPublicos.isEnabled = disponible

    btnCrear.isEnabled =
      disponible && mostrarMisPersonajes

    btnCerrarSesion.isEnabled =
      !cerrandoSesion && !verificandoSesion
  }

  private fun cargarPersonajes() {

    if (sesionValidada && !cerrandoSesion) {

      adapter.actualizarPersonajes(emptyList())

      viewModel.cargarPersonajes(
        mostrarMisPersonajes
      )
    }
  }

  private fun cerrarSesion() {

    if (cerrandoSesion || verificandoSesion) return

    lifecycleScope.launch {

      cerrandoSesion = true

      actualizarBotones()
      actualizarListado()

      txtEstado.text = getString(
        R.string.estado_cerrando_sesion
      )

      try {

        val respuesta = api.logout()

        if (
          respuesta.isSuccessful ||
          respuesta.code() == 401
        ) {

          respuesta.errorBody()?.close()

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
        actualizarListado()
      }
    }
  }
}
