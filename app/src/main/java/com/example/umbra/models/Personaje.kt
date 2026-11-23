package com.example.umbra.models

data class Personaje(
    val idPersonaje: Int,
    val nombre: String,
    val historia: String,
    val personalidad: String,
    val imagen: String?,
    val fuerza: Int,
    val destreza: Int,
    val inteligencia: Int,
    val carisma: Int,
    val visibilidad: String,
    val idCategoria: Int,
    val clase: String?,
    val raza: String?,
    val nivel: Int,
    val origen: String?,
    val rasgos: List<String>?,
    val retrato: Int,
    val favorito: Boolean
)
