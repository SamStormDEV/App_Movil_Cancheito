package com.example.myappcancheito.empleador.ofertas

data class Usuario(
    var uid: String? = null,
    var nombre_completo: String? = null,   // para postulantes
    var nombreComercial: String? = null,   // para empleadores
    var email: String? = null,
    var ubicacion: String? = null,
    var tipoUsuario: String? = null,
    var fotoPerfilUrl: String? = null,
    var cvUrl: String? = null
)