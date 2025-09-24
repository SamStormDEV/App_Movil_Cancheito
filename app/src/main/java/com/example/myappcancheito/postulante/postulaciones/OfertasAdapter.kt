package com.example.myappcancheito.postulante.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Offer
import com.example.myappcancheito.postulante.Postulacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class OfertasAdapter(private val listaOfertas: List<Offer>) :
    RecyclerView.Adapter<OfertasAdapter.OfertaViewHolder>() {

    class OfertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        holder.tvCargo.text = oferta.cargo
        holder.tvDescripcion.text = oferta.descripcion
        holder.tvUbicacion.text = oferta.ubicacion
        holder.tvEstado.text = oferta.estado

        // Acción del botón
        holder.btnPostular.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(holder.itemView.context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbRef = FirebaseDatabase.getInstance().getReference("postulaciones")
            val postId = dbRef.push().key ?: return@setOnClickListener

            val postulacion = Postulacion(
                id = postId,
                ofertaId = oferta.id,
                postulanteId = userId,
                fechaHora = System.currentTimeMillis()
            )

            dbRef.child(postId).setValue(postulacion).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(holder.itemView.context, "Postulación realizada ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(holder.itemView.context, "Error al postular ❌", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = listaOfertas.size
}
