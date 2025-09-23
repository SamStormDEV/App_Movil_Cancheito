package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PostulanteProfile(
    var uid: String? = null,
    var nombre_completo: String? = null,
    var email: String? = null,
    var tipoUsuario: String? = null,
    var tiempo_registro: Any? = null,
    var ubicacion: String? = null,
    var formacion: String? = null,
    var experiencia: String? = null,
    var fotoPerfilUrl: String? = null,
    var cvUrl: String? = null,
    var usuario_verificado: Boolean? = null
)