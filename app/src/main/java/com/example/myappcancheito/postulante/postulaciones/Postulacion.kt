package com.example.myappcancheito.postulante

data class Postulacion(
    var id: String = "",
    var ofertaId: String = "",
    var postulanteId: String = "",
    var fechaHora: Long = System.currentTimeMillis()
)
