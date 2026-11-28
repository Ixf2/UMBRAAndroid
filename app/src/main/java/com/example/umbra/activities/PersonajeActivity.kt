package com.example.umbra.activities

import androidx.compose.ui.res.pluralStringResource
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.umbra.ui.theme.UmbraTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.umbra.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.onFocusChanged
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
                                .safeDrawingPadding()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                modifier = Modifier.padding(top = 16.dp),
                                text = "Cargando personaje...",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    esEdicion && personaje == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .safeDrawingPadding()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mensaje.ifBlank {
                                    "No se puede cargar el personaje"
                                },
                                color = MaterialTheme.colorScheme.onBackground
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

    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val formularioValido = nombre.isNotBlank() && nombre.length <= 50 &&
        historia.isNotBlank() && personalidad.isNotBlank() &&
        clase.length <= 50 && raza.length <= 50 && origen.length <= 100 &&
        listOf(nivel, fuerza, destreza, inteligencia, carisma).all {
            it.toIntOrNull()?.let { numero -> numero in 1..20 } == true
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "UMBRA",
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (personajeInicial == null) {
                "Crear personaje"
            } else {
                "Editar personaje"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.umbra_campos_obligatorios),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )

        SeccionUmbra("Datos generales") {
            CampoTexto("Nombre", nombre, { nombre = it }, 50, !guardando, obligatorio = true, mostrarError = intentoGuardar)
            CampoTexto("Clase", clase, { clase = it }, 50, !guardando, mostrarError = intentoGuardar)
            CampoTexto("Raza", raza, { raza = it }, 50, !guardando, mostrarError = intentoGuardar)
            CampoTexto("Origen", origen, { origen = it }, 100, !guardando, mostrarError = intentoGuardar)
            CampoNumero("Nivel", nivel, !guardando, intentoGuardar) { nivel = it }
        }

        SeccionUmbra("Historia") {
            CampoTexto("Historia", historia, { historia = it }, habilitado = !guardando, lineasMinimas = 3, obligatorio = true, mostrarError = intentoGuardar)
        }

        SeccionUmbra("Personalidad") {
            CampoTexto("Personalidad", personalidad, { personalidad = it }, habilitado = !guardando, lineasMinimas = 3, obligatorio = true, mostrarError = intentoGuardar)
        }

        SeccionUmbra("Estadísticas") {
            CampoNumero("Fuerza", fuerza, !guardando, intentoGuardar) { fuerza = it }
            CampoNumero("Destreza", destreza, !guardando, intentoGuardar) { destreza = it }
            CampoNumero("Inteligencia", inteligencia, !guardando, intentoGuardar) { inteligencia = it }
            CampoNumero("Carisma", carisma, !guardando, intentoGuardar) { carisma = it }
        }

        SeccionUmbra("Visibilidad") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (publico) "Público" else "Privado",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = publico,
                    onCheckedChange = { publico = it },
                    enabled = !guardando
                )
            }
        }

        if (intentoGuardar && !formularioValido) {
            Text(
                text = stringResource(R.string.umbra_revisar_campos),
                color = MaterialTheme.colorScheme.error
            )
        }

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = {

                intentoGuardar = true

                if (formularioValido) {

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
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
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
    maxCaracteres: Int = Int.MAX_VALUE,
    habilitado: Boolean = true,
    lineasMinimas: Int = 1,
    obligatorio: Boolean = false,
    mostrarError: Boolean = false
) {
    var tuvoFoco by rememberSaveable { mutableStateOf(false) }
    var enfocado by rememberSaveable { mutableStateOf(false) }
    val limitado = maxCaracteres != Int.MAX_VALUE
    val errorVacio = obligatorio && valor.isBlank() &&
        (mostrarError || (tuvoFoco && !enfocado))
    val errorMaximo = limitado && valor.length > maxCaracteres

    OutlinedTextField(
        value = valor,
        enabled = habilitado,
        onValueChange = onCambio,
        label = {
            Text(stringResource(
                if (obligatorio) R.string.umbra_etiqueta_obligatoria
                else R.string.umbra_etiqueta_opcional,
                etiqueta
            ))
        },
        isError = errorVacio || errorMaximo,
        supportingText = {
            Column {
                Text(
                    when {
                        errorVacio -> stringResource(R.string.umbra_error_obligatorio)
                        errorMaximo -> pluralStringResource(R.plurals.validacion_error_maximo, maxCaracteres, maxCaracteres)
                        obligatorio && limitado -> stringResource(R.string.validacion_rango_longitud, maxCaracteres)
                        limitado -> pluralStringResource(R.plurals.validacion_maximo_caracteres, maxCaracteres, maxCaracteres)
                        obligatorio -> stringResource(R.string.umbra_minimo_texto)
                        else -> stringResource(R.string.umbra_opcional)
                    }
                )
                Text(
                    text = if (limitado) stringResource(R.string.validacion_contador_limite, valor.length, maxCaracteres)
                    else pluralStringResource(R.plurals.validacion_contador_caracteres, valor.length, valor.length),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        },
        minLines = lineasMinimas,
        modifier = Modifier.fillMaxWidth().onFocusChanged {
            enfocado = it.isFocused
            if (it.isFocused) tuvoFoco = true
        }
    )
}

@Composable
fun CampoNumero(
    etiqueta: String,
    valor: String,
    habilitado: Boolean = true,
    mostrarError: Boolean = false,
    onCambio: (String) -> Unit
) {
    var tuvoFoco by rememberSaveable { mutableStateOf(false) }
    var enfocado by rememberSaveable { mutableStateOf(false) }
    val valido = valor.toIntOrNull()?.let { it in 1..20 } == true
    val error = !valido && (mostrarError || valor.isNotEmpty() || (tuvoFoco && !enfocado))

    OutlinedTextField(
        value = valor,
        enabled = habilitado,
        onValueChange = {
            if (it.all { caracter -> caracter in '0'..'9' }) onCambio(it)
        },
        label = { Text(stringResource(R.string.umbra_etiqueta_obligatoria, etiqueta)) },
        isError = error,
        supportingText = {
            Text(stringResource(if (error && valor.isBlank()) R.string.umbra_error_obligatorio
            else if (error) R.string.umbra_error_numero
            else R.string.umbra_rango_numero))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().onFocusChanged {
            enfocado = it.isFocused
            if (it.isFocused) tuvoFoco = true
        }
    )
}

@Composable
private fun SeccionUmbra(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}
