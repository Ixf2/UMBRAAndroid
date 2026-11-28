
package com.example.umbra.utils

import com.example.umbra.models.PersonajeRequest
import kotlin.random.Random

object GeneradorPersonajes {

  private val nombres = listOf(
    "Nox", "Selene", "Ayla", "Kael",
    "Elara", "Nyxara", "Elysia", "Iriel",
    "Lyra", "Thalion", "Astarion", "Luneth"
  )

  private val apellidos = listOf(
    "Sombraluna", "Cuervo Gris", "Nocturna",
    "Vaelith", "Grim", "Valen",
    "Sombravelo", "de la Bruma",
    "Alba Eterna", "Luzoscura"
  )

  private val clases = listOf(
    "Guerrero", "Mago", "Pícaro",
    "Hechicero", "Paladín", "Druida",
    "Nigromante", "Bárbaro", "Explorador",
    "Clérigo", "Bardo", "Monje"
  )

  private val razas = listOf(
    "Humano", "Elfo", "Semielfo",
    "Enano", "Orco", "Tiefling",
    "Khajita", "Dracónido",
    "Gnomo", "Hada", "Vampiro"
  )

  private val origenes = listOf(
    "Reino de las sombras",
    "Bosque ancestral",
    "Ciudad de cristal",
    "Tierras olvidadas",
    "Montañas del norte",
    "Imperio de Umbra",
    "Valle de la Luna",
    "Santuario de las estrellas"
  )

  private val historias = listOf(
    "La desaparición de su familia le llevó a emprender un viaje en busca de respuestas.",
    "Un antiguo juramento le obliga a recorrer tierras desconocidas.",
    "Oculta un poder ancestral cuyo verdadero origen desconoce.",
    "Busca recuperar una reliquia perdida que perteneció a su familia.",
    "Tras la caída de su reino, decidió enfrentarse a quienes destruyeron su hogar.",
    "Una misteriosa profecía marcó su destino desde su nacimiento.",
    "Abandonó su hogar para descubrir los secretos de una civilización desaparecida."
  )

  private val objetivos = listOf(
    "Encontrar la verdad sobre su pasado.",
    "Proteger a quienes considera su familia.",
    "Recuperar el honor perdido.",
    "Dominar sus poderes antes de que sea demasiado tarde.",
    "Descubrir el origen de una antigua maldición.",
    "Evitar que una fuerza oscura destruya su mundo."
  )

  private val rasgos = listOf(
    "Valiente", "Astuto", "Leal",
    "Misterioso", "Reservado", "Carismático",
    "Inteligente", "Ambicioso", "Prudente",
    "Rebelde", "Perseverante", "Independiente"
  )

  fun generar(): PersonajeRequest {

    val nombre = "${nombres.random()} ${apellidos.random()}"
    val clase = clases.random()
    val raza = razas.random()
    val origen = origenes.random()

    val rasgosSeleccionados = rasgos
      .shuffled()
      .take(3)

    val historia = "${historias.random()} Su objetivo es ${objetivos.random().replaceFirstChar { it.lowercase() }}"

    val personalidad =
      "Destaca por los siguientes rasgos: ${rasgosSeleccionados.joinToString(", ").lowercase()}. Afronta los desafíos con determinación y se mantiene fiel a sus principios."

    return PersonajeRequest(
      nombre = nombre,
      historia = historia,
      personalidad = personalidad,
      imagen = null,
      fuerza = Random.nextInt(1, 21),
      destreza = Random.nextInt(1, 21),
      inteligencia = Random.nextInt(1, 21),
      carisma = Random.nextInt(1, 21),
      visibilidad = "privado",
      idCategoria = 1,
      clase = clase,
      raza = raza,
      nivel = Random.nextInt(1, 21),
      origen = origen,
      rasgos = rasgosSeleccionados,
      retrato = 2,
      favorito = false
    )
  }
}
