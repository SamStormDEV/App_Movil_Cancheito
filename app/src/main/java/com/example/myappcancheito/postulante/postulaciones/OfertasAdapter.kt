package com.example.myappcancheito.postulante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class OfertaAdapter(
    private val listaOfertas: List<Offer>
) : RecyclerView.Adapter<OfertaAdapter.OfertaViewHolder>() {

    inner class OfertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCargo: TextView = view.findViewById(R.id.tvCargoOferta)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionOferta)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacionOferta)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoOferta)
        val btnPostular: Button = view.findViewById(R.id.btnPostular)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfertaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_oferta, parent, false)
        return OfertaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfertaViewHolder, position: Int) {
        val oferta = listaOfertas[position]

        // Evita nulos o strings vacíos
        holder.tvCargo.text = oferta.cargo.ifEmpty { "Sin cargo" }
        holder.tvDescripcion.text = oferta.descripcion.ifEmpty { "Sin descripción" }
        holder.tvUbicacion.text = oferta.ubicacion.ifEmpty { "Ubicación no especificada" }
        holder.tvEstado.text = oferta.estado.ifEmpty { "Sin estado" }

        // Acción al postular
        holder.btnPostular.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: "desconocido"

            val postulacion = mapOf(
                "userId" to userId,
                "ofertaId" to oferta.id,
                "cargo" to oferta.cargo,
                "fechaPostulacion" to System.currentTimeMillis()
            )

            val dbRef = FirebaseDatabase.getInstance().getReference("postulaciones")
            val newId = dbRef.push().key!!

            dbRef.child(newId).setValue(postulacion)
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "¡Postulación enviada!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "Error al postular", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = listaOfertas.size
}
