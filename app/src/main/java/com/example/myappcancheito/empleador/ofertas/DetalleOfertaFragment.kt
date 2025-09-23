package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
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

        val offerId = requireArguments().getString(ARG_OFFER_ID)
        val uid = auth.currentUser?.uid

        if (offerId.isNullOrBlank() || uid.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Faltan datos de la oferta", Toast.LENGTH_SHORT).show()
            return
        }

        // /ofertas/{uid}/{offerId}
        FirebaseDatabase.getInstance().reference
            .child("ofertas").child(uid).child(offerId)
            .get()
            .addOnSuccessListener { snap ->
                val o = snap.getValue(Offer::class.java) ?: return@addOnSuccessListener

                // ===== Datos de la oferta =====
                binding.tvCargo.text = o.cargo
                binding.tvModalidad.text = "Modalidad: ${o.modalidad}"
                binding.tvUbicacion.text = "Ubicación: ${o.ubicacion}"
                binding.tvDescripcion.text = o.descripcion
                binding.tvEstado.text = "Estado: ${o.estado}"

                // ===== Card del empleador (US-004) =====
                binding.tvEmpresaDetalle.text = o.empleadorInfo?.nombreComercial ?: "—"
                binding.tvRubroDetalle.text = o.empleadorInfo?.rubro ?: ""
                binding.tvUbicacionDetalle.text = o.empleadorInfo?.ubicacion ?: ""
                binding.ivLogoEmpresa.load(o.empleadorInfo?.fotoPerfilUrl) {
                    crossfade(true)
                    placeholder(R.mipmap.ic_launcher_round)
                    error(R.mipmap.ic_launcher_round)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
