package com.example.umbra.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.adapters.PersonajeAdapter
import com.example.umbra.network.RetrofitClient
import com.example.umbra.viewmodel.PersonajeViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class BibliotecaActivity : AppCompatActivity() {

  private val api = RetrofitClient.apiService

  private lateinit var viewModel: PersonajeViewModel
  private lateinit var btnCargar: Button
  private lateinit var txtEstado: TextView

  private var mostrarMisPersonajes = false

  private val crearPersonajeLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { resultado ->
    if (resultado.resultCode == RESULT_OK) {
      cargarPersonajes()
    }
  }

  private val adapter = PersonajeAdapter(
    onPersonajeClick = { personaje ->
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
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_biblioteca_activity)

    viewModel = ViewModelProvider(this)[PersonajeViewModel::class.java]

    btnCargar = findViewById(R.id.btnCargarPersonajes)
    txtEstado = findViewById(R.id.txtEstado)

    val btnVolver = findViewById<Button>(R.id.btnVolver)
    val btnIrLogin = findViewById<Button>(R.id.btnIrLogin)
    val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
    val btnCrear = findViewById<Button>(R.id.btnCrearPersonaje)
    val titulo = findViewById<TextView>(R.id.tituloBiblioteca)
    val recycler = findViewById<RecyclerView>(R.id.recyclerPersonajes)

    mostrarMisPersonajes = intent.getBooleanExtra(
      "mostrarMisPersonajes",
      false
    )

    titulo.text = if (mostrarMisPersonajes) {
      "Mis personajes de UMBRA"
    } else {
      "Biblioteca pública de UMBRA"
    }

    btnCargar.text = if (mostrarMisPersonajes) {
      "Cargar mis personajes"
    } else {
      "Cargar personajes públicos"
    }

    btnIrLogin.visibility =
      if (mostrarMisPersonajes) View.GONE else View.VISIBLE

    btnCerrarSesion.visibility =
      if (mostrarMisPersonajes) View.VISIBLE else View.GONE

    btnCrear.visibility =
      if (mostrarMisPersonajes) View.VISIBLE else View.GONE

    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter

    observarViewModel()

    btnCargar.setOnClickListener {
      cargarPersonajes()
    }

    btnCrear.setOnClickListener {
      val intent = Intent(
        this,
        PersonajeActivity::class.java
      )

      crearPersonajeLauncher.launch(intent)
    }

    btnVolver.setOnClickListener {
      finish()
    }

    btnIrLogin.setOnClickListener {
      val intent = Intent(
        this,
        MainActivity::class.java
      )

      intent.flags =
        Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP

      startActivity(intent)
      finish()
    }

    btnCerrarSesion.setOnClickListener {
      lifecycleScope.launch {
        btnCerrarSesion.isEnabled = false
        txtEstado.text = "Cerrando sesión..."

        try {
          val respuesta = api.logout()

          if (respuesta.isSuccessful) {
            val intent = Intent(
              this@BibliotecaActivity,
              MainActivity::class.java
            )

            intent.flags =
              Intent.FLAG_ACTIVITY_NEW_TASK or
                  Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
          } else {
            txtEstado.text =
              "Error al cerrar sesión: ${respuesta.code()}"
          }

        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          txtEstado.text =
            "Error de conexión: ${e.localizedMessage}"
        } finally {
          btnCerrarSesion.isEnabled = true
        }
      }
    }

    cargarPersonajes()
  }


  private fun observarViewModel() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {

        launch {
          viewModel.personajes.collect { personajes ->
            adapter.actualizarPersonajes(personajes)
          }
        }

        launch {
          viewModel.mensaje.collect { mensaje ->
            txtEstado.text = mensaje
          }
        }

        launch {
          viewModel.cargando.collect { cargando ->
            btnCargar.isEnabled = !cargando
          }
        }
      }
    }
  }


  private fun cargarPersonajes() {
    viewModel.cargarPersonajes(mostrarMisPersonajes)
  }
}
