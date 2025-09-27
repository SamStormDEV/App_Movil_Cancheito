package com.example.myappcancheito.postulante.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.databinding.ItemOfferBinding
import com.example.myappcancheito.empleador.ofertas.Offer



class OfferAdapter(
    private val onClick: (Offer) -> Unit
) : ListAdapter<Offer, OfferAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(o: Offer, n: Offer) = o.id == n.id
        override fun areContentsTheSame(o: Offer, n: Offer) = o == n
    }

    inner class VH(val b: ItemOfferBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Offer) = with(b) {
            tvTitulo.text = item.cargo
            tvEmpresaCiudad.text = "${item.ubicacion} • ${item.modalidad}"
            tvCategoria.text = "Pago aprox: ${item.pago_aprox}"
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
