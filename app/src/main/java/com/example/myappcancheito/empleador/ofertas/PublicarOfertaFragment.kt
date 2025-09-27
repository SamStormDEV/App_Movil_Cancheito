package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPublicarOfertaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class PublicarOfertaFragment : Fragment(R.layout.fragment_publicar_oferta) {

    private var _binding: FragmentPublicarOfertaBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPublicarOfertaBinding.bind(view)

        val opciones = listOf("Presencial", "Híbrido", "Remoto")
        binding.actvModalidad.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opciones)
        )

        binding.btnPublicar.setOnClickListener { publicar() }
    }

    private fun publicar() {
        val cargo = binding.etCargo.text?.toString()?.trim().orEmpty()
        val descripcion = binding.etDescripcion.text?.toString()?.trim().orEmpty()
        val modalidad = binding.actvModalidad.text?.toString()?.trim().orEmpty()
        val ubicacion = binding.etUbicacion.text?.toString()?.trim().orEmpty()

        var ok = true
        if (cargo.isEmpty()) { binding.tilCargo.error = "Requerido"; ok = false } else binding.tilCargo.error = null
        if (descripcion.isEmpty()) { binding.tilDescripcion.error = "Requerido"; ok = false } else binding.tilDescripcion.error = null
        if (modalidad.isEmpty()) { binding.tilModalidad.error = "Requerido"; ok = false } else binding.tilModalidad.error = null
        if (ubicacion.isEmpty()) { binding.tilUbicacion.error = "Requerido"; ok = false } else binding.tilUbicacion.error = null
        if (!ok) return

        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val id = UUID.randomUUID().toString()
        val offer = Offer(
            id = id,
            employerId = uid,
            cargo = cargo,
            descripcion = descripcion,
            modalidad = modalidad,
            ubicacion = ubicacion,
            estado = "ACTIVA"
        )

        db.child("ofertas").child(uid).child(id)
            .setValue(offer)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Oferta publicada", Toast.LENGTH_SHORT).show()
                // Opcional: volver o ir a Mis Ofertas
                // parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}