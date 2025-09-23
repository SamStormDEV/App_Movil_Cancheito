package com.example.myappcancheito.empleador

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPerfilEmpleadorBinding
import com.example.myappcancheito.empleador.model.EmpleadorProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FragmentPerfilEmpleador : Fragment(R.layout.fragment_perfil_empleador) {

    private var _binding: FragmentPerfilEmpleadorBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val rtdb by lazy { FirebaseDatabase.getInstance().reference }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private var selectedImageUri: Uri? = null
    private var fotoActualUrl: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivFoto.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilEmpleadorBinding.bind(view)

        binding.btnCambiarFoto.setOnClickListener { pickImage.launch("image/*") }
        binding.btnGuardar.setOnClickListener { guardarPerfil() }

        cargarPerfilSiExiste()
    }

    private fun cargarPerfilSiExiste() = viewLifecycleOwner.lifecycleScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            binding.progress.visibility = View.VISIBLE
            val snap = rtdb.child("empleadores").child(uid).get().await()
            val perfil = snap.getValue(EmpleadorProfile::class.java)
            perfil?.let {
                fotoActualUrl = it.fotoPerfilUrl
                binding.ivFoto.load(it.fotoPerfilUrl)
                binding.etNombreComercial.setText(it.nombreComercial ?: "")
                binding.etRubro.setText(it.rubro ?: "")
                binding.etDescripcion.setText(it.descripcion ?: "")
                binding.etUbicacion.setText(it.ubicacion ?: "")
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error cargando perfil: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            binding.progress.visibility = View.GONE
        }
    }

    private fun EditText.requerido(): String? {
        val s = text?.toString()?.trim()
        if (s.isNullOrEmpty()) {
            error = "Obligatorio"; requestFocus()
            return null
        }
        error = null
        return s
    }

    private fun guardarPerfil() = viewLifecycleOwner.lifecycleScope.launch {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_LONG).show()
            return@launch
        }

        // Validaciones obligatorias
        val nombre = binding.etNombreComercial.requerido() ?: return@launch
        val rubro = binding.etRubro.requerido() ?: return@launch
        val desc = binding.etDescripcion.requerido() ?: return@launch
        val ubic = binding.etUbicacion.requerido() ?: return@launch

        binding.progress.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        try {
            // 1) Subimos foto si el usuario eligió una nueva
            val urlFoto = if (selectedImageUri != null) {
                val ref = storage.getReference("perfiles_empleador/$uid/foto.jpg")
                ref.putFile(selectedImageUri!!).await()
                ref.downloadUrl.await().toString()
            } else {
                fotoActualUrl // mantenemos la anterior (puede ser null si nunca tuvo)
            }

            // 2) Guardamos el perfil en RTDB
            val perfil = EmpleadorProfile(
                uid = uid,
                nombreComercial = nombre,
                rubro = rubro,
                descripcion = desc,
                ubicacion = ubic,
                fotoPerfilUrl = urlFoto,
                actualizadoEn = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                rtdb.child("empleadores").child(uid).setValue(perfil).await()
            }

            fotoActualUrl = urlFoto
            selectedImageUri = null
            Toast.makeText(requireContext(), "Perfil guardado", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo guardar: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            binding.progress.visibility = View.GONE
            binding.btnGuardar.isEnabled = true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
