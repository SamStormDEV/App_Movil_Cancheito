package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentDetalleOfertaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetalleOfertaFragment : Fragment(R.layout.fragment_detalle_oferta) {

    companion object {
        private const val ARG_OFFER_ID = "offerId"
        fun newInstance(offerId: String) = DetalleOfertaFragment().apply {
            arguments = Bundle().apply { putString(ARG_OFFER_ID, offerId) }
        }
    }

    private var _binding: FragmentDetalleOfertaBinding? = null
    private val binding get() = _binding!!
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetalleOfertaBinding.bind(view)

        val offerId = requireArguments().getString(ARG_OFFER_ID) ?: return
        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance().reference
            .child("ofertas").child(uid).child(offerId)
            .get()
            .addOnSuccessListener { snap ->
                val o = snap.getValue(Offer::class.java) ?: return@addOnSuccessListener
                binding.tvCargo.text = o.cargo
                binding.tvModalidad.text = "Modalidad: ${o.modalidad}"
                binding.tvUbicacion.text = "Ubicación: ${o.ubicacion}"
                binding.tvDescripcion.text = o.descripcion
                binding.tvEstado.text = "Estado: ${o.estado}"
            }
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}