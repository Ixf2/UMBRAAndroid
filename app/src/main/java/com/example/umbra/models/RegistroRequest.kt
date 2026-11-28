package com.example.umbra.models

data class RegistroRequest(
  val nombreUsuario: String,
  val email: String,
  val password: String
)