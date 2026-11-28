
package com.example.umbra.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.models.Personaje
import com.example.umbra.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonajeAdapter(
    private val personajes: MutableList<Personaje> = mutableListOf(),
    private val onPersonajeClick: (Personaje) -> Unit = {}
) : RecyclerView.Adapter<PersonajeAdapter.PersonajeViewHolder>() {

    private val api = RetrofitClient.apiService

    class PersonajeViewHolder(
        itemView: View,
        val lifecycleOwner: LifecycleOwner?
    ) : RecyclerView.ViewHolder(itemView) {

        val nombre: TextView =
            itemView.findViewById(R.id.txtNombrePersonaje)

        val claseRaza: TextView =
            itemView.findViewById(R.id.txtClaseRaza)

        val nivelVisibilidad: TextView =
            itemView.findViewById(R.id.txtNivelVisibilidad)

        val estadisticas: TextView =
            itemView.findViewById(R.id.txtEstadisticas)

        val imagen: ImageView =
            itemView.findViewById(R.id.imgPersonaje)

        val iniciales: TextView =
            itemView.findViewById(R.id.txtInicialesPersonaje)

        var trabajoImagen: Job? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonajeViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personaje, parent, false)

        return PersonajeViewHolder(
            vista,
            parent.findViewTreeLifecycleOwner()
                ?: parent.context as? LifecycleOwner
        )
    }

    override fun onBindViewHolder(
        holder: PersonajeViewHolder,
        position: Int
    ) {

        val personaje = personajes[position]
        val context = holder.itemView.context

        holder.nombre.text = personaje.nombre

        holder.claseRaza.text = context.getString(
            R.string.personaje_clase_raza,
            personaje.clase ?: context.getString(R.string.sin_clase),
            personaje.raza ?: context.getString(R.string.sin_raza)
        )

        holder.nivelVisibilidad.text = context.getString(
            R.string.personaje_nivel_visibilidad,
            personaje.nivel,
            personaje.visibilidad
        )

        holder.estadisticas.text = context.getString(
            R.string.personaje_estadisticas,
            personaje.fuerza,
            personaje.destreza,
            personaje.inteligencia,
            personaje.carisma
        )

        cargarImagen(holder, personaje)

        holder.itemView.setOnClickListener {
            onPersonajeClick(personaje)
        }
    }

    private fun cargarImagen(
        holder: PersonajeViewHolder,
        personaje: Personaje
    ) {
        Log.d(
            "UMBRA_IMAGEN",
            "Entrando en cargarImagen: ${personaje.idPersonaje}"
        )

        holder.trabajoImagen?.cancel()

        holder.imagen.setImageDrawable(null)
        holder.imagen.visibility = View.INVISIBLE

        holder.iniciales.text = personaje.nombre
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") {
                it.take(1).uppercase()
            }

        holder.iniciales.visibility = View.VISIBLE

        val retratoPredeterminado = when (personaje.retrato) {
            0 -> R.drawable.elara_valen
            1 -> R.drawable.kael_grim
            else -> null
        }

        if (retratoPredeterminado != null) {

            holder.imagen.setImageResource(
                retratoPredeterminado
            )

            holder.imagen.visibility = View.VISIBLE
            holder.iniciales.visibility = View.INVISIBLE
        }

        val lifecycleOwner = holder.lifecycleOwner

        if (lifecycleOwner == null) {

            Log.e(
                "UMBRA_IMAGEN",
                "No se encontró LifecycleOwner"
            )

            return
        }

        holder.trabajoImagen =
            lifecycleOwner.lifecycleScope.launch {

                try {

                    val bitmap: Bitmap? = withContext(Dispatchers.IO) {

                        Log.d(
                            "UMBRA_IMAGEN",
                            "Solicitando imagen ID: ${personaje.idPersonaje}"
                        )

                        val respuesta = api.obtenerMiniatura(
                            personaje.idPersonaje
                        )

                        Log.d(
                            "UMBRA_IMAGEN",
                            "ID: ${personaje.idPersonaje} | HTTP: ${respuesta.code()}"
                        )

                        if (respuesta.isSuccessful) {

                            respuesta.body()?.use { cuerpo ->

                                val bytes = cuerpo.bytes()

                                Log.d(
                                    "UMBRA_IMAGEN",
                                    "Bytes recibidos: ${bytes.size}"
                                )

                                BitmapFactory.decodeByteArray(
                                    bytes,
                                    0,
                                    bytes.size
                                )
                            }

                        } else {

                            respuesta.errorBody()?.close()
                            null
                        }
                    }

                    if (bitmap != null) {

                        holder.imagen.setImageBitmap(bitmap)
                        holder.imagen.visibility = View.VISIBLE
                        holder.iniciales.visibility = View.INVISIBLE

                        Log.d(
                            "UMBRA_IMAGEN",
                            "Imagen cargada correctamente: ${personaje.idPersonaje}"
                        )
                    }

                } catch (e: CancellationException) {

                    throw e

                } catch (e: Exception) {

                    Log.e(
                        "UMBRA_IMAGEN",
                        "Error al cargar imagen ID: ${personaje.idPersonaje}",
                        e
                    )
                }
            }
    }

    override fun onViewRecycled(
        holder: PersonajeViewHolder
    ) {

        holder.trabajoImagen?.cancel()
        holder.trabajoImagen = null

        holder.imagen.setImageDrawable(null)
        holder.imagen.visibility = View.INVISIBLE

        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int =
        personajes.size

    fun actualizarPersonajes(
        nuevosPersonajes: List<Personaje>
    ) {

        val cantidadAnterior = personajes.size

        personajes.clear()

        if (cantidadAnterior > 0) {

            notifyItemRangeRemoved(
                0,
                cantidadAnterior
            )
        }

        personajes.addAll(nuevosPersonajes)

        if (personajes.isNotEmpty()) {

            notifyItemRangeInserted(
                0,
                personajes.size
            )
        }
    }
}
