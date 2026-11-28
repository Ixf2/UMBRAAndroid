
package com.example.umbra.activities

import com.example.umbra.ui.theme.UmbraTheme
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.viewmodel.FormularioPersonajeViewModel

class PersonajeActivity : ComponentActivity() {

    private lateinit var viewModel: FormularioPersonajeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[
            FormularioPersonajeViewModel::class.java
        ]

        val idPersonaje = intent.getIntExtra("idPersonaje", -1)
        val esEdicion = idPersonaje != -1

        viewModel.cargarPersonaje(idPersonaje)

        setContent {
            UmbraTheme {

                val personaje by viewModel.personaje.collectAsState()
                val cargando by viewModel.cargando.collectAsState()
                val guardando by viewModel.guardando.collectAsState()
                val mensaje by viewModel.mensaje.collectAsState()
                val guardado by viewModel.guardado.collectAsState()

                LaunchedEffect(guardado) {
                    if (guardado) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }

                when {
                    cargando -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF101518))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cargando personaje...",
                                color = Color.White
                            )
                        }
                    }

                    esEdicion && personaje == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF101518))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mensaje.ifBlank {
                                    "No se puede cargar el personaje"
                                },
                                color = Color.White
                            )

                            TextButton(
                                onClick = { finish() }
                            ) {
                                Text("Volver")
                            }
                        }
                    }

                    else -> {
                        FormularioPersonaje(
                            personajeInicial = personaje,
                            guardando = guardando,
                            mensaje = mensaje,
                            onVolver = {
                                finish()
                            },
                            onGuardar = { datos ->
                                viewModel.guardarPersonaje(
                                    idPersonaje,
                                    datos
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioPersonaje(
    personajeInicial: Personaje? = null,
    guardando: Boolean,
    mensaje: String,
    onVolver: () -> Unit,
    onGuardar: (PersonajeRequest) -> Unit
) {

    var nombre by rememberSaveable {
        mutableStateOf(personajeInicial?.nombre.orEmpty())
    }

    var historia by rememberSaveable {
        mutableStateOf(personajeInicial?.historia.orEmpty())
    }

    var personalidad by rememberSaveable {
        mutableStateOf(personajeInicial?.personalidad.orEmpty())
    }

    var clase by rememberSaveable {
        mutableStateOf(personajeInicial?.clase.orEmpty())
    }

    var raza by rememberSaveable {
        mutableStateOf(personajeInicial?.raza.orEmpty())
    }

    var origen by rememberSaveable {
        mutableStateOf(personajeInicial?.origen.orEmpty())
    }

    var nivel by rememberSaveable {
        mutableStateOf(
            (personajeInicial?.nivel ?: 1).toString()
        )
    }

    var fuerza by rememberSaveable {
        mutableStateOf(
            (personajeInicial?.fuerza ?: 10).toString()
        )
    }

    var destreza by rememberSaveable {
        mutableStateOf(
            (personajeInicial?.destreza ?: 10).toString()
        )
    }

    var inteligencia by rememberSaveable {
        mutableStateOf(
            (personajeInicial?.inteligencia ?: 10).toString()
        )
    }

    var carisma by rememberSaveable {
        mutableStateOf(
            (personajeInicial?.carisma ?: 10).toString()
        )
    }

    var publico by rememberSaveable {
        mutableStateOf(
            personajeInicial?.visibilidad == "publico"
        )
    }

    var errorFormulario by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101518))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "UMBRA",
            fontSize = 30.sp,
            color = Color.White
        )

        Text(
            text = if (personajeInicial == null) {
                "Crear personaje"
            } else {
                "Editar personaje"
            },
            fontSize = 22.sp,
            color = Color.White
        )

        CampoTexto(
            "Nombre",
            nombre,
            { nombre = it },
            50
        )

        CampoTexto(
            "Historia",
            historia,
            { historia = it }
        )

        CampoTexto(
            "Personalidad",
            personalidad,
            { personalidad = it }
        )

        CampoTexto(
            "Clase",
            clase,
            { clase = it },
            50
        )

        CampoTexto(
            "Raza",
            raza,
            { raza = it },
            50
        )

        CampoTexto(
            "Origen",
            origen,
            { origen = it },
            100
        )

        CampoNumero("Nivel", nivel) {
            nivel = it
        }

        Text(
            text = "Estadísticas",
            fontSize = 20.sp,
            color = Color.White
        )

        CampoNumero("Fuerza", fuerza) {
            fuerza = it
        }

        CampoNumero("Destreza", destreza) {
            destreza = it
        }

        CampoNumero("Inteligencia", inteligencia) {
            inteligencia = it
        }

        CampoNumero("Carisma", carisma) {
            carisma = it
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = if (publico) {
                    "Visibilidad: Público"
                } else {
                    "Visibilidad: Privado"
                },
                color = Color.White
            )

            Switch(
                checked = publico,
                onCheckedChange = {
                    publico = it
                },
                enabled = !guardando
            )
        }

        if (errorFormulario.isNotBlank()) {
            Text(
                text = errorFormulario,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = Color(0xFFC4CFD5)
            )
        }

        Button(
            onClick = {

                val estadisticas = listOf(
                    nivel.toIntOrNull(),
                    fuerza.toIntOrNull(),
                    destreza.toIntOrNull(),
                    inteligencia.toIntOrNull(),
                    carisma.toIntOrNull()
                )

                errorFormulario = when {

                    nombre.isBlank() ->
                        "Introduce el nombre del personaje"

                    historia.isBlank() ->
                        "Introduce una historia"

                    personalidad.isBlank() ->
                        "Introduce la personalidad"

                    estadisticas.any {
                        it == null || it !in 1..20
                    } ->
                        "El nivel y las estadísticas deben estar entre 1 y 20"

                    else -> ""
                }

                if (errorFormulario.isEmpty()) {

                    onGuardar(
                        PersonajeRequest(
                            nombre = nombre.trim(),
                            historia = historia.trim(),
                            personalidad = personalidad.trim(),
                            imagen = personajeInicial?.imagen,
                            fuerza = fuerza.toInt(),
                            destreza = destreza.toInt(),
                            inteligencia = inteligencia.toInt(),
                            carisma = carisma.toInt(),
                            visibilidad = if (publico) {
                                "publico"
                            } else {
                                "privado"
                            },
                            idCategoria =
                                personajeInicial?.idCategoria ?: 1,
                            clase = clase.trim().ifBlank { null },
                            raza = raza.trim().ifBlank { null },
                            nivel = nivel.toInt(),
                            origen = origen.trim().ifBlank { null },
                            rasgos =
                                personajeInicial?.rasgos.orEmpty(),
                            retrato =
                                personajeInicial?.retrato ?: 2,
                            favorito =
                                personajeInicial?.favorito ?: false
                        )
                    )
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                when {
                    guardando ->
                        "Guardando..."

                    personajeInicial != null ->
                        "Actualizar personaje"

                    else ->
                        "Guardar personaje"
                }
            )
        }

        TextButton(
            onClick = onVolver,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar y volver")
        }
    }
}

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    maxCaracteres: Int = Int.MAX_VALUE
) {

    OutlinedTextField(
        value = valor,
        onValueChange = {
            if (it.length <= maxCaracteres) {
                onCambio(it)
            }
        },
        label = {
            Text(etiqueta)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CampoNumero(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit
) {

    OutlinedTextField(
        value = valor,
        onValueChange = {
            if (it.length <= 2 && it.all(Char::isDigit)) {
                onCambio(it)
            }
        },
        label = {
            Text(etiqueta)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
