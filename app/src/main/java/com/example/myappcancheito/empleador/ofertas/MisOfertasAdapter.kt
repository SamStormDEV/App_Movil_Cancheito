package com.example.myappcancheito.empleador.ofertas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R

class MisOfertasAdapter(
    private val data: MutableList<Offer>,
    private val onClick: (Offer) -> Unit
) : RecyclerView.Adapter<MisOfertasAdapter.VH>() {

    class VH(v: View): RecyclerView.ViewHolder(v) {
        val tvCargo: TextView = v.findViewById(R.id.tvCargo)
        val tvEstado: TextView = v.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_oferta, parent, false)
        return VH(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = data[pos]
        h.tvCargo.text = item.cargo
        h.tvEstado.text = "Estado: ${item.estado}"
        h.itemView.setOnClickListener { onClick(item) }
    }

    fun setItems(items: List<Offer>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}