package com.example.umbra.models

data class PersonajeRequest(
    val nombre: String,
    val historia: String,
    val personalidad: String,
    val imagen: String? = null,
    val fuerza: Int,
    val destreza: Int,
    val inteligencia: Int,
    val carisma: Int,
    val visibilidad: String,
    val idCategoria: Int,
    val clase: String? = null,
    val raza: String? = null,
    val nivel: Int,
    val origen: String? = null,
    val rasgos: List<String> = emptyList(),
    val retrato: Int = 2,
    val favorito: Boolean = false
)
