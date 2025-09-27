package com.example.myappcancheito.empleador.ofertas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Postulacion
import com.example.myappcancheito.empleador.ofertas.Usuario

// Unimos datos: el Usuario + su Postulacion
data class ItemPostulante(val usuario: Usuario, val postulacion: Postulacion)

class PostulanteAdapter(
    private val onClick: (ItemPostulante) -> Unit
) : ListAdapter<ItemPostulante, PostulanteAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postulante, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        itemView: View,
        private val onClick: (ItemPostulante) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombre)
        private val tvUbicacion = itemView.findViewById<TextView>(R.id.tvUbicacion)
        private val tvRubro = itemView.findViewById<TextView>(R.id.tvRubro)

        fun bind(item: ItemPostulante) {
            val u = item.usuario
            tvNombre.text = u.nombre_completo ?: u.nombreComercial ?: "(Sin nombre)"
            tvUbicacion.text = "Ubicación: ${u.ubicacion ?: "-"}"

            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ItemPostulante>() {
            override fun areItemsTheSame(oldItem: ItemPostulante, newItem: ItemPostulante): Boolean {
                return oldItem.usuario.uid == newItem.usuario.uid
            }
            override fun areContentsTheSame(oldItem: ItemPostulante, newItem: ItemPostulante): Boolean {
                return oldItem == newItem
            }
        }
    }
}
