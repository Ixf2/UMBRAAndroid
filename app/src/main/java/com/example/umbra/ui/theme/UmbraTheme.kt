
package com.example.umbra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.umbra.R

@Composable
fun UmbraTheme(
  content: @Composable () -> Unit
) {

  val colores = darkColorScheme(
    primary = colorResource(R.color.umbra_primary),
    onPrimary = colorResource(R.color.umbra_on_primary),
    primaryContainer = colorResource(R.color.umbra_primary_dark),
    onPrimaryContainer = colorResource(R.color.umbra_text),
    secondary = colorResource(R.color.umbra_text_secondary),
    secondaryContainer = colorResource(R.color.umbra_surface_variant),
    onSecondaryContainer = colorResource(R.color.umbra_text),
    background = colorResource(R.color.umbra_background),
    onBackground = colorResource(R.color.umbra_text),
    surface = colorResource(R.color.umbra_surface),
    onSurface = colorResource(R.color.umbra_text),
    surfaceVariant = colorResource(R.color.umbra_surface_variant),
    onSurfaceVariant = colorResource(R.color.umbra_text_secondary),
    outline = colorResource(R.color.umbra_border),
    error = colorResource(R.color.umbra_error),
    onError = colorResource(R.color.umbra_background)
  )

  MaterialTheme(
    colorScheme = colores,
    content = content
  )
}
