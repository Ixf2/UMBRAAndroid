
package com.example.umbra.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.umbra.R
import com.example.umbra.models.LoginRequest
import com.example.umbra.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val api = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBiblioteca = findViewById<Button>(R.id.btnBiblioteca)
        val txtEstado = findViewById<TextView>(R.id.txtLoginEstado)

        btnBiblioteca.setOnClickListener {
            abrirBiblioteca(false)
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (
                email.isBlank() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                etEmail.error = getString(
                    R.string.login_correo_invalido
                )

                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isBlank()) {

                etPassword.error = getString(
                    R.string.login_password_obligatoria
                )

                etPassword.requestFocus()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                btnLogin.isEnabled = false
                btnBiblioteca.isEnabled = false

                txtEstado.text = getString(
                    R.string.login_procesando
                )

                try {

                    val respuesta = api.login(
                        LoginRequest(email, password)
                    )

                    if (respuesta.isSuccessful) {

                        val usuario = respuesta.body()

                        if (usuario != null) {

                            txtEstado.text = getString(
                                R.string.login_bienvenida,
                                usuario.nombreUsuario
                            )

                            etPassword.text.clear()

                            abrirBiblioteca(true)

                        } else {

                            txtEstado.text = getString(
                                R.string.login_sin_datos
                            )
                        }

                    } else {

                        txtEstado.text = when (respuesta.code()) {

                            400, 401 -> getString(
                                R.string.login_credenciales_incorrectas
                            )

                            403 -> getString(
                                R.string.login_acceso_denegado
                            )

                            else -> getString(
                                R.string.estado_error_http,
                                respuesta.code()
                            )
                        }
                    }

                } catch (e: CancellationException) {

                    throw e

                } catch (e: Exception) {

                    txtEstado.text = getString(
                        R.string.estado_error_conexion,
                        e.localizedMessage.orEmpty()
                    )

                } finally {

                    btnLogin.isEnabled = true
                    btnBiblioteca.isEnabled = true
                }
            }
        }
    }

    private fun abrirBiblioteca(
        mostrarMisPersonajes: Boolean
    ) {

        val intent = Intent(
            this,
            BibliotecaActivity::class.java
        )

        intent.putExtra(
            "mostrarMisPersonajes",
            mostrarMisPersonajes
        )

        startActivity(intent)
    }
}
