package com.example.umbra.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.umbra.R
import com.example.umbra.models.Personaje

class PersonajeAdapter(
    private val personajes: MutableList<Personaje> = mutableListOf(),
    private val onPersonajeClick: (Personaje) -> Unit = {}
) : RecyclerView.Adapter<PersonajeAdapter.PersonajeViewHolder>() {

    class PersonajeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nombre: TextView =
            itemView.findViewById(R.id.txtNombrePersonaje)

        val claseRaza: TextView =
            itemView.findViewById(R.id.txtClaseRaza)

        val nivelVisibilidad: TextView =
            itemView.findViewById(R.id.txtNivelVisibilidad)

        val estadisticas: TextView =
            itemView.findViewById(R.id.txtEstadisticas)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonajeViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personaje, parent, false)

        return PersonajeViewHolder(vista)
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
            personaje.clase ?: "Sin clase",
            personaje.raza ?: "Sin raza"
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

        holder.itemView.setOnClickListener {
            onPersonajeClick(personaje)
        }
    }

    override fun getItemCount(): Int = personajes.size

    fun actualizarPersonajes(nuevosPersonajes: List<Personaje>) {
        val cantidadAnterior = personajes.size

        personajes.clear()

        if (cantidadAnterior > 0) {
            notifyItemRangeRemoved(0, cantidadAnterior)
        }

        personajes.addAll(nuevosPersonajes)

        if (personajes.isNotEmpty()) {
            notifyItemRangeInserted(0, personajes.size)
        }
    }
}