
package com.example.umbra.activities

import android.app.Activity
import android.app.AlertDialog
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
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.viewmodel.DetallePersonajeViewModel
import kotlinx.coroutines.launch

class DetallePersonajeActivity : AppCompatActivity() {

  private lateinit var viewModel: DetallePersonajeViewModel
  private lateinit var txtEstado: TextView
  private lateinit var btnEditar: Button
  private lateinit var btnEliminar: Button

  private var idPersonaje = -1
  private var puedeEditar = false

  private val editarLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { resultado ->
    if (resultado.resultCode == Activity.RESULT_OK) {
      setResult(Activity.RESULT_OK)
      viewModel.cargarPersonaje(idPersonaje)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detalle_personaje)

    viewModel = ViewModelProvider(this)[
      DetallePersonajeViewModel::class.java
    ]

    idPersonaje = intent.getIntExtra("idPersonaje", -1)
    puedeEditar = intent.getBooleanExtra("puedeEditar", false)

    txtEstado = findViewById(R.id.txtEstadoDetalle)
    btnEditar = findViewById(R.id.btnEditarPersonaje)
    btnEliminar = findViewById(R.id.btnEliminarPersonaje)

    val btnVolver = findViewById<Button>(
      R.id.btnVolverDetalle
    )

    btnEditar.visibility = View.GONE
    btnEliminar.visibility = View.GONE

    btnVolver.setOnClickListener {
      finish()
    }

    btnEditar.setOnClickListener {
      if (puedeEditar && idPersonaje != -1) {
        val intent = Intent(
          this,
          PersonajeActivity::class.java
        )

        intent.putExtra("idPersonaje", idPersonaje)
        editarLauncher.launch(intent)
      }
    }

    btnEliminar.setOnClickListener {
      if (puedeEditar && idPersonaje != -1) {
        confirmarEliminacion()
      }
    }

    if (idPersonaje == -1) {
      txtEstado.text = getString(
        R.string.detalle_personaje_invalido
      )
      return
    }

    observarViewModel()

    if (savedInstanceState == null) {
      viewModel.cargarPersonaje(idPersonaje)
    }
  }

  private fun observarViewModel() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {

        launch {
          viewModel.personaje.collect { personaje ->
            if (personaje != null) {
              mostrarPersonaje(personaje)
            } else {
              btnEditar.visibility = View.GONE
              btnEliminar.visibility = View.GONE
            }
          }
        }

        launch {
          viewModel.mensaje.collect { mensaje ->
            txtEstado.text = mensaje
          }
        }

        launch {
          viewModel.cargando.collect { cargando ->
            actualizarBotones()
          }
        }

        launch {
          viewModel.eliminando.collect { eliminando ->
            actualizarBotones()
          }
        }
      }
    }
  }

  private fun actualizarBotones() {
    val disponible =
      viewModel.personaje.value != null &&
          !viewModel.cargando.value &&
          !viewModel.eliminando.value

    val visible = puedeEditar && disponible

    btnEditar.visibility =
      if (visible) View.VISIBLE else View.GONE

    btnEliminar.visibility =
      if (visible) View.VISIBLE else View.GONE

    btnEditar.isEnabled = disponible
    btnEliminar.isEnabled = disponible
  }

  private fun mostrarPersonaje(personaje: Personaje) {

    findViewById<TextView>(
      R.id.txtNombreDetalle
    ).text = personaje.nombre

    findViewById<TextView>(
      R.id.txtClaseRazaDetalle
    ).text = getString(
      R.string.personaje_clase_raza,
      personaje.clase ?: getString(R.string.sin_clase),
      personaje.raza ?: getString(R.string.sin_raza)
    )

    findViewById<TextView>(
      R.id.txtNivelDetalle
    ).text = getString(
      R.string.personaje_nivel_visibilidad,
      personaje.nivel,
      personaje.visibilidad
    )

    findViewById<TextView>(
      R.id.txtHistoriaDetalle
    ).text = personaje.historia

    findViewById<TextView>(
      R.id.txtPersonalidadDetalle
    ).text = personaje.personalidad

    findViewById<TextView>(
      R.id.txtEstadisticasDetalle
    ).text = getString(
      R.string.detalle_valores_estadisticas,
      personaje.fuerza,
      personaje.destreza,
      personaje.inteligencia,
      personaje.carisma
    )

    actualizarBotones()
  }

  private fun confirmarEliminacion() {
    val personaje = viewModel.personaje.value ?: return

    AlertDialog.Builder(this)
      .setTitle(R.string.eliminar_confirmacion_titulo)
      .setMessage(
        getString(
          R.string.eliminar_confirmacion_mensaje,
          personaje.nombre
        )
      )
      .setNegativeButton(
        R.string.eliminar_cancelar,
        null
      )
      .setPositiveButton(
        R.string.eliminar_confirmar
      ) { _, _ ->
        eliminarPersonaje()
      }
      .show()
  }

  private fun eliminarPersonaje() {
    lifecycleScope.launch {
      val eliminado = viewModel.eliminarPersonaje(
        idPersonaje
      )

      if (eliminado) {
        setResult(Activity.RESULT_OK)
        finish()
      }
    }
  }
}
