package com.example.umbra.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.adapters.PersonajeAdapter
import com.example.umbra.network.RetrofitClient
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class BibliotecaActivity : AppCompatActivity() {

    private val repository = PersonajeRepository()
    private val api = RetrofitClient.apiService
    private val adapter = PersonajeAdapter()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca_activity)

        btnCargar = findViewById(R.id.btnCargarPersonajes)
        txtEstado = findViewById(R.id.txtEstado)

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnIrLogin = findViewById<Button>(R.id.btnIrLogin)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val btnCrear = findViewById<Button>(R.id.btnCrearPersonaje)
        val titulo = findViewById<TextView>(R.id.tituloBiblioteca)
        val recycler = findViewById<RecyclerView>(R.id.recyclerPersonajes)

        mostrarMisPersonajes =
            intent.getBooleanExtra("mostrarMisPersonajes", false)

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

        btnCargar.setOnClickListener {
            cargarPersonajes()
        }

        btnCrear.setOnClickListener {
            val intent = Intent(this, PersonajeActivity::class.java)
            crearPersonajeLauncher.launch(intent)
        }

        btnVolver.setOnClickListener {
            finish()
        }

        btnIrLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

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

    private fun cargarPersonajes() {
        lifecycleScope.launch {
            btnCargar.isEnabled = false
            txtEstado.text = "Cargando personajes..."

            try {
                val respuesta = if (mostrarMisPersonajes) {
                    repository.obtenerMisPersonajes()
                } else {
                    repository.obtenerPublicos()
                }

                if (respuesta.isSuccessful) {
                    val personajes = respuesta.body().orEmpty()

                    adapter.actualizarPersonajes(personajes)

                    txtEstado.text = if (personajes.isEmpty()) {
                        "No hay personajes disponibles."
                    } else {
                        "Personajes encontrados: ${personajes.size}"
                    }
                } else {
                    adapter.actualizarPersonajes(emptyList())

                    txtEstado.text = when (respuesta.code()) {
                        401, 403 -> "Debes iniciar sesión"
                        else -> "Error HTTP: ${respuesta.code()}"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                txtEstado.text =
                    "Error de conexión: ${e.localizedMessage}"
            } finally {
                btnCargar.isEnabled = true
            }
        }
    }
}
