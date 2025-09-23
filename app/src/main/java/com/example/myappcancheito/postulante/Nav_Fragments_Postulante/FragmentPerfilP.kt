package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPerfilPostulanteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentPerfilP : Fragment(R.layout.fragment_perfil_postulante) {

    private var _binding: FragmentPerfilPostulanteBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedPdfUri: Uri? = null
    private var removePhoto: Boolean = false
    private var removeCv: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            removePhoto = false
            binding.ivFoto.load(uri)
        }
    }

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            removeCv = false
            binding.tvCvActual.text = "CV seleccionado: ${uri.lastPathSegment ?: "archivo.pdf"}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilPostulanteBinding.bind(view)
        binding.progress.visibility = View.VISIBLE
        cargarPerfil()

        binding.btnSeleccionarFoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnQuitarFoto.setOnClickListener {
            selectedImageUri = null
            removePhoto = true
            binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
        }

        binding.btnSeleccionarCv.setOnClickListener { pickPdfLauncher.launch("application/pdf") }
        binding.btnQuitarCv.setOnClickListener {
            selectedPdfUri = null
            removeCv = true
            binding.tvCvActual.text = getString(R.string.perfil_cv)
            guardarCambios()
        }

        binding.btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun cargarPerfil() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(requireContext(), "Inicia sesión", Toast.LENGTH_LONG).show()
            _binding?.progress?.visibility = View.GONE
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
        ref.get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.progress.visibility = View.GONE
            if (!snapshot.exists()) {
                binding.tvNombre.text = "Nombre: ${user.displayName ?: ""}"
                binding.tvEmail.text = "Correo: ${user.email ?: ""}"
                binding.tvTipo.text = "Tipo: "
                binding.tvRegistro.text = "Registrado: "
                binding.etUbicacion.setText("")
                binding.etFormacion.setText("")
                binding.etExperiencia.setText("")
                binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
                binding.tvCvActual.text = getString(R.string.perfil_cv)
                binding.tvNoVerificado.visibility = View.VISIBLE
                removePhoto = false
                removeCv = false
                return@addOnSuccessListener
            }

            val perfil = snapshot.getValue(PostulanteProfile::class.java)
            binding.tvNombre.text = "Nombre: ${perfil?.nombre_completo ?: ""}"
            binding.tvEmail.text = "Correo: ${perfil?.email ?: ""}"
            binding.tvTipo.text = "Tipo: ${perfil?.tipoUsuario ?: ""}"
            binding.tvRegistro.text = "Registrado: ${formatearTiempo(perfil?.tiempo_registro) ?: ""}"
            binding.etUbicacion.setText(perfil?.ubicacion ?: "")
            binding.etFormacion.setText(perfil?.formacion ?: "")
            binding.etExperiencia.setText(perfil?.experiencia ?: "")
            perfil?.fotoPerfilUrl?.let { binding.ivFoto.load(it) } ?: binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
            binding.tvCvActual.text = if (perfil?.cvUrl != null) "CV: disponible" else getString(R.string.perfil_cv)
            binding.tvNoVerificado.visibility = if (perfil?.usuario_verificado == true) View.GONE else View.VISIBLE
            removePhoto = false
            removeCv = false
        }.addOnFailureListener { e ->
            if (_binding == null) return@addOnFailureListener
            binding.progress.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambios() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(requireContext(), "Inicia sesión", Toast.LENGTH_LONG).show()
            return
        }

        val ubicacion = binding.etUbicacion.text.toString().trim()
        val formacion = binding.etFormacion.text.toString().trim()
        val experiencia = binding.etExperiencia.text.toString().trim()

        binding.progress.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val storage = FirebaseStorage.getInstance().reference
                var fotoUrlSubida: String? = null
                var cvUrlSubido: String? = null

                withContext(Dispatchers.IO) {
                    if (selectedImageUri != null) {
                        val fotoRef = storage.child("perfiles/${user.uid}/foto_${System.currentTimeMillis()}.jpg")
                        fotoRef.putFile(selectedImageUri!!).await()
                        fotoUrlSubida = fotoRef.downloadUrl.await().toString()
                    }

                    if (selectedPdfUri != null) {
                        val cvRef = storage.child("perfiles/${user.uid}/cv_${System.currentTimeMillis()}.pdf")
                        cvRef.putFile(selectedPdfUri!!).await()
                        cvUrlSubido = cvRef.downloadUrl.await().toString()
                    }
                }

                val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
                val snapshot = ref.get().await()

                val updates = mutableMapOf<String, Any?>(
                    "ubicacion" to (if (ubicacion.isBlank()) null else ubicacion),
                    "formacion" to (if (formacion.isBlank()) null else formacion),
                    "experiencia" to (if (experiencia.isBlank()) null else experiencia)
                )

                if (fotoUrlSubida != null) updates["fotoPerfilUrl"] = fotoUrlSubida
                else if (removePhoto) updates["fotoPerfilUrl"] = null

                if (cvUrlSubido != null) updates["cvUrl"] = cvUrlSubido
                else if (removeCv) updates["cvUrl"] = null

                if (!snapshot.exists()) {
                    val nuevo = PostulanteProfile(
                        uid = user.uid,
                        nombre_completo = user.displayName,
                        email = user.email,
                        tipoUsuario = "postulante",
                        tiempo_registro = System.currentTimeMillis(),
                        ubicacion = updates["ubicacion"] as String?,
                        formacion = updates["formacion"] as String?,
                        experiencia = updates["experiencia"] as String?,
                        fotoPerfilUrl = updates["fotoPerfilUrl"] as String?,
                        cvUrl = updates["cvUrl"] as String?,
                        usuario_verificado = false
                    )
                    ref.setValue(nuevo).await()
                } else {
                    ref.updateChildren(updates).await()
                }

                if (_binding == null) return@launch
                binding.progress.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                selectedImageUri = null
                selectedPdfUri = null
                removePhoto = false
                removeCv = false
                cargarPerfil()
            } catch (e: Exception) {
                if (_binding == null) return@launch
                binding.progress.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formatearTiempo(valor: Any?): String {
        return when (valor) {
            is Number -> SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(valor.toLong()))
            is String -> valor
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}