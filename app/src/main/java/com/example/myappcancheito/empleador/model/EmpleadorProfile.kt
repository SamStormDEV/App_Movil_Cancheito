package com.example.myappcancheito.empleador.model

data class EmpleadorProfile(
    val uid: String? = null,
    val nombreComercial: String? = null,
    val rubro: String? = null,
    val descripcion: String? = null,
    val ubicacion: String? = null,
    val fotoPerfilUrl: String? = null,
    val actualizadoEn: Long? = null
)

// Mapper: de perfil completo a snapshot para ofertas
fun EmpleadorProfile.toInfo() = EmpleadorInfo(
    uid = uid,
    nombreComercial = nombreComercial,
    rubro = rubro,
    ubicacion = ubicacion,
    fotoPerfilUrl = fotoPerfilUrl
)
