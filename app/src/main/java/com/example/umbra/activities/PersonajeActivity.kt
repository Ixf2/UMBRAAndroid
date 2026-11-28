package com.example.umbra.activities

import androidx.compose.ui.res.pluralStringResource
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.umbra.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.umbra.utils.SessionManager
import kotlinx.coroutines.launch
import com.example.umbra.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.onFocusChanged
import com.example.umbra.models.Personaje
import com.example.umbra.models.PersonajeRequest
import com.example.umbra.viewmodel.FormularioPersonajeViewModel

class PersonajeActivity : ComponentActivity() {

  private lateinit var viewModel: FormularioPersonajeViewModel
  private var idPersonaje = -1
  private var estadoSesion by mutableStateOf<SessionManager.EstadoSesion?>(null)
  private var formularioInicializado by mutableStateOf(false)
  private var verificandoSesion = false

  override fun onStart() {
    super.onStart()
    if ((!formularioInicializado || !viewModel.guardando.value) && !viewModel.guardado.value) {
      verificarSesion()
    }
  }

  private fun verificarSesion(alValidar: (() -> Unit)? = null) {
    if (verificandoSesion || isFinishing) return

    verificandoSesion = true
    estadoSesion = null

    lifecycleScope.launch {
      try {
        val estado = SessionManager.verificarSesion()
        estadoSesion = estado

        when (estado) {
          SessionManager.EstadoSesion.AUTENTICADA -> {
            if (!formularioInicializado) {
              if (!viewModel.guardando.value && !viewModel.datosGuardados.value) {
                viewModel.cargarPersonaje(idPersonaje)
              }
              formularioInicializado = true
            }
            alValidar?.invoke()
          }

          SessionManager.EstadoSesion.NO_AUTENTICADA -> {
            SessionManager.volverLogin(this@PersonajeActivity)
          }

          SessionManager.EstadoSesion.ERROR_CONEXION,
          SessionManager.EstadoSesion.ERROR_SERVIDOR -> Unit
        }
      } finally {
        verificandoSesion = false
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this)[
      FormularioPersonajeViewModel::class.java
    ]

    idPersonaje = intent.getIntExtra("idPersonaje", -1)
    val esEdicion = idPersonaje != -1

    setContent {
      UmbraTheme {

        val personaje by viewModel.personaje.collectAsState()
        val cargando by viewModel.cargando.collectAsState()
        val guardando by viewModel.guardando.collectAsState()
        val mensaje by viewModel.mensaje.collectAsState()
        val guardado by viewModel.guardado.collectAsState()
        val datosGuardados by viewModel.datosGuardados.collectAsState()
        val subiendoImagen by viewModel.subiendoImagen.collectAsState()

        BackHandler(enabled = guardando || datosGuardados) {
          if (!guardando) {
            setResult(Activity.RESULT_OK)
            finish()
          }
        }

        LaunchedEffect(guardado) {
          if (guardado) {
            setResult(Activity.RESULT_OK)
            finish()
          }
        }

        val sesionValida = estadoSesion == SessionManager.EstadoSesion.AUTENTICADA
        val foco = LocalFocusManager.current

        LaunchedEffect(sesionValida) {
          if (!sesionValida) foco.clearFocus(force = true)
        }

        Box(modifier = Modifier.fillMaxSize()) {
          if (formularioInicializado) {
            Box(
              modifier = Modifier
                  .fillMaxSize()
                  .then(
                      if (sesionValida) Modifier
                      else Modifier
                          .alpha(0f)
                          .clearAndSetSemantics { }
                  )
            ) {
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
                      text = stringResource(R.string.detalle_cargando_personaje),
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
                      onClick = { finish() },
                      enabled = sesionValida
                    ) {
                      Text("Volver")
                    }
                  }
                }

                else -> {
                  FormularioPersonaje(
                    personajeInicial = personaje,
                    sesionHabilitada = sesionValida,
                    guardando = guardando,
                    datosGuardados = datosGuardados,
                    subiendoImagen = subiendoImagen,
                    mensaje = mensaje,
                    onVolver = {
                      if (datosGuardados) setResult(Activity.RESULT_OK)
                      finish()
                    },
                    onGuardar = { datos, archivo ->
                      if (sesionValida && !viewModel.guardando.value) {
                        verificarSesion {
                          viewModel.guardarPersonaje(
                            idPersonaje,
                            datos,
                            archivo
                          )
                        }
                      }
                    }
                  )
                }
              }
            }
          }

          if (!sesionValida) {
            Column(
              modifier = Modifier
                  .fillMaxSize()
                  .background(MaterialTheme.colorScheme.background)
                  .safeDrawingPadding()
                  .padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              if (estadoSesion == null || estadoSesion == SessionManager.EstadoSesion.NO_AUTENTICADA) {
                CircularProgressIndicator()
                Text(stringResource(R.string.estado_verificando_sesion))
              } else {
                Text(
                  text = stringResource(
                    if (estadoSesion == SessionManager.EstadoSesion.ERROR_CONEXION)
                      R.string.sesion_error_conexion
                    else R.string.sesion_error_servidor
                  ),
                  textAlign = TextAlign.Center
                )
                Button(onClick = { verificarSesion() }) {
                  Text(stringResource(R.string.sesion_reintentar))
                }
                TextButton(onClick = {
                  if (datosGuardados) setResult(Activity.RESULT_OK)
                  finish()
                }) {
                  Text(stringResource(R.string.detalle_volver_biblioteca))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun FormularioPersonaje(
  personajeInicial: Personaje? = null,
  sesionHabilitada: Boolean = true,
  guardando: Boolean,
  datosGuardados: Boolean,
  subiendoImagen: Boolean,
  mensaje: String,
  onVolver: () -> Unit,
  onGuardar: (PersonajeRequest, MultipartBody.Part?) -> Unit
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

  val contexto = LocalContext.current
  val mensajeErrorLectura = stringResource(
    R.string.imagen_error_lectura
  )
  var uriImagen by rememberSaveable { mutableStateOf<String?>(null) }
  var revisionImagen by remember { mutableStateOf(0) }
  var imagenPreparada by remember { mutableStateOf<ImagenPreparada?>(null) }
  var imagenActual by remember { mutableStateOf<Bitmap?>(null) }
  var errorImagen by remember { mutableStateOf("") }
  var leyendoImagen by remember { mutableStateOf(false) }
  val camposHabilitados = sesionHabilitada && !guardando && !datosGuardados
  val selectorImagen = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      imagenPreparada = null
      errorImagen = ""
      uriImagen = uri.toString()
      revisionImagen++
    }
  }

  LaunchedEffect(uriImagen, revisionImagen) {
    imagenPreparada = null
    errorImagen = ""
    val seleccion = uriImagen
    leyendoImagen = seleccion != null
    if (seleccion != null) {
      try {
        imagenPreparada = withContext(Dispatchers.IO) {
          prepararImagen(contexto, Uri.parse(seleccion))
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: IllegalArgumentException) {
        errorImagen = e.message ?: mensajeErrorLectura
      } catch (e: Exception) {
        errorImagen = mensajeErrorLectura
      } finally {
        leyendoImagen = false
      }
    }
  }

  LaunchedEffect(personajeInicial?.idPersonaje) {
    val id = personajeInicial?.idPersonaje ?: return@LaunchedEffect
    try {
      imagenActual = withContext(Dispatchers.IO) {
        val respuesta = RetrofitClient.apiService.obtenerImagenDetalle(id)
        if (respuesta.isSuccessful) {
          respuesta.body()?.use { BitmapFactory.decodeStream(it.byteStream()) }
        } else {
          respuesta.errorBody()?.close()
          null
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      imagenActual = null
    }
  }

  val seleccionLista = uriImagen == null || imagenPreparada?.uri == uriImagen


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

    SeccionUmbra(stringResource(R.string.imagen_retrato)) {
      val vistaPrevia = imagenPreparada?.bitmap ?: imagenActual
      val retrato = when (personajeInicial?.retrato) {
        0 -> R.drawable.elara_valen
        1 -> R.drawable.kael_grim
        else -> null
      }
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        val marco = Modifier
            .size(144.dp, 192.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
        when {
          vistaPrevia != null -> Image(
            bitmap = vistaPrevia.asImageBitmap(),
            contentDescription = stringResource(R.string.imagen_vista_previa),
            modifier = marco,
            contentScale = ContentScale.Crop
          )

          retrato != null -> Image(
            painter = painterResource(retrato),
            contentDescription = stringResource(R.string.imagen_vista_previa),
            modifier = marco,
            contentScale = ContentScale.Crop
          )

          else -> Box(marco, contentAlignment = Alignment.Center) {
            Text(
              nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                .take(2).joinToString("") { it.take(1).uppercase() },
              fontSize = 32.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
      Button(
        onClick = { selectorImagen.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
        enabled = sesionHabilitada && !guardando && !leyendoImagen,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          stringResource(
            if (uriImagen != null || imagenActual != null || retrato != null)
              R.string.imagen_cambiar else R.string.imagen_seleccionar
          )
        )
      }
      if (uriImagen != null) {
        TextButton(
          onClick = {
            uriImagen = null
            imagenPreparada = null
            errorImagen = ""
          },
          enabled = sesionHabilitada && !guardando && !leyendoImagen
        ) {
          Text(stringResource(R.string.imagen_descartar_seleccion))
        }
      }
      Text(stringResource(R.string.imagen_requisitos), fontSize = 13.sp)
      if (leyendoImagen || subiendoImagen) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
      }
      if (errorImagen.isNotBlank()) {
        Text(errorImagen, color = MaterialTheme.colorScheme.error)
      }
    }

    SeccionUmbra("Datos generales") {
      CampoTexto(
        "Nombre",
        nombre,
        { nombre = it },
        50,
        camposHabilitados,
        obligatorio = true,
        mostrarError = intentoGuardar
      )
      CampoTexto(
        "Clase",
        clase,
        { clase = it },
        50,
        camposHabilitados,
        mostrarError = intentoGuardar
      )
      CampoTexto("Raza", raza, { raza = it }, 50, camposHabilitados, mostrarError = intentoGuardar)
      CampoTexto(
        "Origen",
        origen,
        { origen = it },
        100,
        camposHabilitados,
        mostrarError = intentoGuardar
      )
      CampoNumero("Nivel", nivel, camposHabilitados, intentoGuardar) { nivel = it }
    }

    SeccionUmbra("Historia") {
      CampoTexto(
        "Historia",
        historia,
        { historia = it },
        habilitado = camposHabilitados,
        lineasMinimas = 3,
        obligatorio = true,
        mostrarError = intentoGuardar
      )
    }

    SeccionUmbra("Personalidad") {
      CampoTexto(
        "Personalidad",
        personalidad,
        { personalidad = it },
        habilitado = camposHabilitados,
        lineasMinimas = 3,
        obligatorio = true,
        mostrarError = intentoGuardar
      )
    }

    SeccionUmbra("Estadísticas") {
      CampoNumero("Fuerza", fuerza, camposHabilitados, intentoGuardar) { fuerza = it }
      CampoNumero("Destreza", destreza, camposHabilitados, intentoGuardar) { destreza = it }
      CampoNumero("Inteligencia", inteligencia, camposHabilitados, intentoGuardar) {
        inteligencia = it
      }
      CampoNumero("Carisma", carisma, camposHabilitados, intentoGuardar) { carisma = it }
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
          enabled = camposHabilitados
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
            ),
            imagenPreparada?.archivo
          )
        }
      },
      enabled = sesionHabilitada && !guardando && !leyendoImagen && seleccionLista && errorImagen.isBlank(),
      modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 56.dp)
    ) {

      Text(
        when {
          subiendoImagen ->
            stringResource(R.string.imagen_subiendo)

          guardando ->
            stringResource(R.string.formulario_guardando_personaje)

          datosGuardados ->
            stringResource(R.string.imagen_reintentar)

          personajeInicial != null ->
            "Actualizar personaje"

          else ->
            "Guardar personaje"
        }
      )
    }

    TextButton(
      onClick = onVolver,
      enabled = sesionHabilitada && !guardando,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(if (datosGuardados) stringResource(R.string.imagen_volver_guardado) else "Cancelar y volver")
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
      Text(
        stringResource(
          if (obligatorio) R.string.umbra_etiqueta_obligatoria
          else R.string.umbra_etiqueta_opcional,
          etiqueta
        )
      )
    },
    isError = errorVacio || errorMaximo,
    supportingText = {
      Column {
        Text(
          when {
            errorVacio -> stringResource(R.string.umbra_error_obligatorio)
            errorMaximo -> pluralStringResource(
              R.plurals.validacion_error_maximo,
              maxCaracteres,
              maxCaracteres
            )

            obligatorio && limitado -> stringResource(
              R.string.validacion_rango_longitud,
              maxCaracteres
            )

            limitado -> pluralStringResource(
              R.plurals.validacion_maximo_caracteres,
              maxCaracteres,
              maxCaracteres
            )

            obligatorio -> stringResource(R.string.umbra_minimo_texto)
            else -> stringResource(R.string.umbra_opcional)
          }
        )
        Text(
          text = if (limitado) stringResource(
            R.string.validacion_contador_limite,
            valor.length,
            maxCaracteres
          )
          else pluralStringResource(
            R.plurals.validacion_contador_caracteres,
            valor.length,
            valor.length
          ),
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.End
        )
      }
    },
    minLines = lineasMinimas,
    modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged {
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
      Text(
        stringResource(
          if (error && valor.isBlank()) R.string.umbra_error_obligatorio
          else if (error) R.string.umbra_error_numero
          else R.string.umbra_rango_numero
        )
      )
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged {
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


private data class ImagenPreparada(
  val uri: String,
  val bitmap: Bitmap,
  val archivo: MultipartBody.Part
)

private fun prepararImagen(contexto: Context, uri: Uri): ImagenPreparada {
  val limite = 5 * 1024 * 1024
  val bytes = contexto.contentResolver.openInputStream(uri)?.use { entrada ->
    val salida = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
      val leidos = entrada.read(buffer)
      if (leidos == -1) break
      total += leidos
      require(total <= limite) { contexto.getString(R.string.imagen_error_tamano) }
      salida.write(buffer, 0, leidos)
    }
    salida.toByteArray()
  } ?: throw IllegalArgumentException(contexto.getString(R.string.imagen_error_lectura))

  val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
  val tipo = opciones.outMimeType
  val extension = when (tipo) {
    "image/png" -> "png"
    "image/jpeg" -> "jpg"
    "image/webp" -> "webp"
    else -> throw IllegalArgumentException(contexto.getString(R.string.imagen_error_formato))
  }
  val ancho = opciones.outWidth
  val alto = opciones.outHeight
  require(
    ancho > 0 && alto > 0 && ancho <= 5000 && alto <= 5000 &&
        ancho.toLong() * alto <= 12_000_000L
  ) {
    contexto.getString(R.string.imagen_error_resolucion)
  }
  var muestra = 1
  while (maxOf(ancho, alto) / muestra > 768) muestra *= 2
  val bitmap = BitmapFactory.decodeByteArray(
    bytes, 0, bytes.size,
    BitmapFactory.Options().apply { inSampleSize = muestra })
    ?: throw IllegalArgumentException(contexto.getString(R.string.imagen_error_procesamiento))
  val cuerpo = bytes.toRequestBody(requireNotNull(tipo).toMediaType())
  val archivo = MultipartBody.Part.createFormData("archivo", "retrato.$extension", cuerpo)
  return ImagenPreparada(uri.toString(), bitmap, archivo)
}
