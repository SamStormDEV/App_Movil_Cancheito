package com.example.myappcancheito.empleador.ofertas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myappcancheito.R

class MisOfertasAdapter(
    private val data: MutableList<Offer>,
    private val onClick: (Offer) -> Unit
) : RecyclerView.Adapter<MisOfertasAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivLogo: ImageView = v.findViewById(R.id.ivLogo)
        val tvCargo: TextView = v.findViewById(R.id.tvCargo)              // ya existía
        val tvEmpresa: TextView = v.findViewById(R.id.tvEmpresa)
        val tvRubroUbicacion: TextView = v.findViewById(R.id.tvRubroUbicacion)
        val tvModalidad: TextView = v.findViewById(R.id.tvModalidad)
        val tvEstado: TextView = v.findViewById(R.id.tvEstado)            // ya existía
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_oferta, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = data[pos]

        // Cargo
        h.tvCargo.text = item.cargo

        // Datos del empleador (US-004)
        h.tvEmpresa.text = item.empleadorInfo?.nombreComercial ?: "—"
        h.tvRubroUbicacion.text = listOfNotNull(
            item.empleadorInfo?.rubro,
            item.empleadorInfo?.ubicacion
        ).joinToString(" • ")

        // Logo (si no hay URL, se mantiene el ícono por defecto)
        h.ivLogo.load(item.empleadorInfo?.fotoPerfilUrl)

        // Modalidad
        h.tvModalidad.text = item.modalidad

        // Estado (chip)
        h.tvEstado.text = item.estado
        when (item.estado.uppercase()) {
            "ACTIVA" -> {
                h.tvEstado.setBackgroundResource(R.drawable.bg_chip_activa)
                h.tvEstado.setTextColor(0xFF0B5F26.toInt())
            }
            "PAUSADA" -> {
                h.tvEstado.setBackgroundResource(R.drawable.bg_chip_neutro)
                h.tvEstado.setTextColor(0xFF7A5C00.toInt())
            }
            else -> { // CERRADA / otro
                h.tvEstado.setBackgroundResource(R.drawable.bg_chip_neutro)
                h.tvEstado.setTextColor(0xFF8A0E0E.toInt())
            }
        }

        h.itemView.setOnClickListener { onClick(item) }
    }

    fun setItems(items: List<Offer>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}
