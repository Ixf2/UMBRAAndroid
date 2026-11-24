
package com.example.umbra.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DetallePersonajeActivity : AppCompatActivity() {

  private val repository = PersonajeRepository()

  private var idPersonaje = -1
  private var puedeEditar = false

  private lateinit var txtEstado: TextView
  private lateinit var btnEditar: Button

  private val editarLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { resultado ->
    if (resultado.resultCode == Activity.RESULT_OK) {
      setResult(Activity.RESULT_OK)
      cargarPersonaje()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detalle_personaje)

    idPersonaje = intent.getIntExtra("idPersonaje", -1)
    puedeEditar = intent.getBooleanExtra("puedeEditar", false)

    txtEstado = findViewById(R.id.txtEstadoDetalle)
    btnEditar = findViewById(R.id.btnEditarPersonaje)

    val btnEliminar = findViewById<Button>(
      R.id.btnEliminarPersonaje
    )

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

    if (idPersonaje == -1) {
      txtEstado.text = getString(
        R.string.detalle_personaje_invalido
      )
      return
    }

    cargarPersonaje()
  }

  private fun cargarPersonaje() {
    btnEditar.visibility = View.GONE

    lifecycleScope.launch {
      txtEstado.text = getString(
        R.string.detalle_cargando_personaje
      )

      try {
        val respuesta = repository.obtenerPersonaje(
          idPersonaje
        )

        if (respuesta.isSuccessful) {
          val personaje = respuesta.body()

          if (personaje != null) {
            mostrarPersonaje(personaje)

            btnEditar.visibility =
              if (puedeEditar) {
                View.VISIBLE
              } else {
                View.GONE
              }

            txtEstado.text = ""
          } else {
            txtEstado.text = getString(
              R.string.detalle_no_encontrado
            )
          }
        } else {
          txtEstado.text = when (respuesta.code()) {
            401 -> getString(
              R.string.detalle_sesion_requerida
            )

            403 -> getString(
              R.string.detalle_sin_permisos
            )

            404 -> getString(
              R.string.detalle_no_encontrado
            )

            else -> getString(
              R.string.detalle_error_http,
              respuesta.code()
            )
          }
        }

      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        txtEstado.text = getString(
          R.string.detalle_error_conexion,
          e.localizedMessage ?: ""
        )
      }
    }
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
  }
}
