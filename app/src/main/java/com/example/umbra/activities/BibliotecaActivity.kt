package com.example.umbra.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.adapters.PersonajeAdapter
import com.example.umbra.repository.PersonajeRepository
import kotlinx.coroutines.launch

class BibliotecaActivity : AppCompatActivity() {

    private val repository = PersonajeRepository()
    private val adapter = PersonajeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca_activity)

        val btnCargar = findViewById<Button>(R.id.btnCargarPersonajes)
        val txtEstado = findViewById<TextView>(R.id.txtEstado)
        val recycler = findViewById<RecyclerView>(R.id.recyclerPersonajes)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnCargar.setOnClickListener {
            lifecycleScope.launch {
                btnCargar.isEnabled = false
                txtEstado.text = "Cargando personajes..."

                try {
                    val respuesta = repository.obtenerPublicos()

                    if (respuesta.isSuccessful) {
                        val personajes = respuesta.body().orEmpty()

                        adapter.actualizarPersonajes(personajes)

                        txtEstado.text = if (personajes.isEmpty()) {
                            "No hay personajes públicos disponibles."
                        } else {
                            "Personajes públicos encontrados: ${personajes.size}"
                        }
                    } else {
                        txtEstado.text = "Error HTTP: ${respuesta.code()}"
                    }
                } catch (e: Exception) {
                    txtEstado.text = "Error de conexión: ${e.localizedMessage}"
                } finally {
                    btnCargar.isEnabled = true
                }
            }
        }
    }
}
