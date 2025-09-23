package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPublicarOfertaBinding
import com.example.myappcancheito.empleador.model.EmpleadorProfile
import com.example.myappcancheito.empleador.model.toInfo
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

    /** === Helpers de validación === **/
    private fun perfilIncompleto(p: EmpleadorProfile?): Boolean {
        return p == null ||
                p.nombreComercial.isNullOrBlank() ||
                p.rubro.isNullOrBlank() ||
                p.descripcion.isNullOrBlank() ||
                p.ubicacion.isNullOrBlank()
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

        // (Opcional) deshabilitar botón / mostrar loader si tienes uno
        binding.btnPublicar.isEnabled = false

        // 1) Cargar perfil de empleador
        db.child("empleadores").child(uid).get()
            .addOnSuccessListener { snap ->
                val perfil = snap.getValue(EmpleadorProfile::class.java)

                // 2) Validar perfil completo
                if (perfilIncompleto(perfil)) {
                    binding.btnPublicar.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Completa tu perfil (nombre comercial, rubro, descripción y ubicación) antes de publicar.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                // 3) Crear oferta con snapshot del empleador
                val id = UUID.randomUUID().toString()
                val offer = Offer(
                    id = id,
                    employerId = uid,
                    cargo = cargo,
                    descripcion = descripcion,
                    modalidad = modalidad,
                    ubicacion = ubicacion,
                    estado = "ACTIVA",
                    empleadorInfo = perfil!!.toInfo()
                )

                // 4) Guardar (mantengo tu estructura /ofertas/{uid}/{id})
                db.child("ofertas").child(uid).child(id)
                    .setValue(offer)
                    .addOnSuccessListener {
                        binding.btnPublicar.isEnabled = true
                        Toast.makeText(requireContext(), "Oferta publicada", Toast.LENGTH_SHORT).show()
                        // parentFragmentManager.popBackStack() // si quieres volver
                    }
                    .addOnFailureListener {
                        binding.btnPublicar.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                binding.btnPublicar.isEnabled = true
                Toast.makeText(requireContext(), "No se pudo leer tu perfil: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
