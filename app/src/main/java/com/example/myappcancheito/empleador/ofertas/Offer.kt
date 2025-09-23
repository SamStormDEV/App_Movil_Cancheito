
package com.example.myappcancheito.empleador.ofertas

import com.example.myappcancheito.empleador.model.EmpleadorInfo

data class Offer(
    var id: String = "",
    var employerId: String = "",
    var cargo: String = "",
    var descripcion: String = "",
    var modalidad: String = "",   // Presencial | Híbrido | Remoto
    var ubicacion: String = "",
    var estado: String = "ACTIVA",
    var createdAt: Long = System.currentTimeMillis(),
    var empleadorInfo: EmpleadorInfo? = null   // <-- NUEVO
)
