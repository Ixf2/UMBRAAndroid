package com.example.umbra.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.umbra.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBiblioteca = findViewById<Button>(R.id.btnBiblioteca)

        btnBiblioteca.setOnClickListener {
            val intent = Intent(this, BibliotecaActivity::class.java)
            startActivity(intent)
        }
    }
}
