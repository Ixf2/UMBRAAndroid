package com.example.umbra.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PersonajeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PantallaPersonaje()
            }
        }
    }
}

@Composable
fun PantallaPersonaje() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101518))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "UMBRA",
            color = Color.White,
            fontSize = 32.sp
        )

        Text(
            text = "Crear o editar personaje",
            color = Color(0xFFC4CFD5),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Formulario en desarrollo",
            color = Color(0xFF9BA7AE),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
