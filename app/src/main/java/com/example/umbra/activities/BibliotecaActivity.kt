
package com.example.umbra.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.umbra.R
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.launch

class BibliotecaActivity : AppCompatActivity() {

    private val repository = PersonajeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca_activity)

        val btnCargar = findViewById<Button>(R.id.btnCargarPersonajes)
        val txtEstado = findViewById<TextView>(R.id.txtEstado)
        val txtPersonajes = findViewById<TextView>(R.id.txtPersonajes)

        btnCargar.setOnClickListener {
            lifecycleScope.launch {
                btnCargar.isEnabled = false
                txtEstado.text = "Consultando Spring Boot..."
                txtPersonajes.text = ""

                try {
                    val respuesta = repository.obtenerPublicos()

                    if (respuesta.isSuccessful) {
                        val personajes = respuesta.body().orEmpty()

                        txtEstado.text =
                            "Personajes públicos encontrados: ${personajes.size}"

                        txtPersonajes.text = if (personajes.isEmpty()) {
                            "No hay personajes públicos disponibles."
                        } else {
                            personajes.joinToString("\n\n") { personaje ->
                                "${personaje.nombre}\n" +
                                        "Clase: ${personaje.clase ?: "Sin clase"}\n" +
                                        "Raza: ${personaje.raza ?: "Sin raza"}\n" +
                                        "Nivel: ${personaje.nivel}"
                            }
                        }
                    } else {
                        txtEstado.text =
                            "Error HTTP: ${respuesta.code()}"
                    }
                } catch (e: Exception) {
                    txtEstado.text =
                        "Error de conexión: ${e.localizedMessage}"
                } finally {
                    btnCargar.isEnabled = true
                }
            }
        }
    }
}
