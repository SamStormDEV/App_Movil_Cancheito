package com.example.myappcancheito.empleador.Nav_fragment_Empleador

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPerfilEmBinding
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

class FragmentPerfilEm : Fragment(R.layout.fragment_perfil_em) {

    private var _binding: FragmentPerfilEmBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedPdfUri: Uri? = null
    private var removePhoto: Boolean = false
    private var removeCv: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            selectedImageUri = uri
            removePhoto = false
            binding.ivFoto.load(uri)
        }
    }

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            selectedPdfUri = uri
            removeCv = false
            binding.tvCvActual.text = "CV seleccionado: ${uri.lastPathSegment ?: "archivo.pdf"}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilEmBinding.bind(view)
        binding.progress.visibility = View.VISIBLE
        cargarPerfil()

        binding.btnSeleccionarFoto.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pickImageLauncher.launch("image/*")
        }

        binding.btnQuitarFoto.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedImageUri = null
            removePhoto = true
            binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
            actualizarFotoPerfilNull()
        }

        binding.btnSeleccionarCv.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pickPdfLauncher.launch("application/pdf")
        }

        binding.btnQuitarCv.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedPdfUri = null
            removeCv = true
            binding.tvCvActual.text = getString(R.string.perfil_cv)
            guardarCambios()
        }

        binding.btnGuardar.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(requireContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            guardarCambios()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun actualizarFotoPerfilNull() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(requireContext(), "Inicia sesión", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
                ref.updateChildren(mapOf("fotoPerfilUrl" to null)).await()
                if (_binding == null) return@launch
                Toast.makeText(requireContext(), "Foto de perfil eliminada", Toast.LENGTH_SHORT).show()
                cargarPerfil()
            } catch (e: Exception) {
                if (_binding == null) return@launch
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
                binding.etNombreCompleto.setText(user.displayName ?: "")
                binding.etEmail.setText(user.email ?: "")
                binding.etTipo.setText("")
                binding.etRegistro.setText("")
                binding.etUbicacion.setText("")
                binding.etNombreComercial.setText("")
                binding.etRubro.setText("")
                binding.etFormacion.setText("")
                binding.etExperiencia.setText("")
                binding.etDescripcion.setText("")
                binding.etSitioWeb.setText("")
                binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
                binding.tvCvActual.text = getString(R.string.perfil_cv)
                binding.tvNoVerificado.visibility = View.VISIBLE
                removePhoto = false
                removeCv = false
                return@addOnSuccessListener
            }

            val perfil = snapshot.getValue(EmpleadorProfile::class.java)
            binding.etNombreCompleto.setText(perfil?.nombre_completo ?: "")
            binding.etEmail.setText(perfil?.email ?: "")
            binding.etTipo.setText(perfil?.tipoUsuario ?: "")
            binding.etRegistro.setText(formatearTiempo(perfil?.tiempo_registro) ?: "")
            binding.etUbicacion.setText(perfil?.ubicacion ?: "")
            binding.etNombreComercial.setText(perfil?.nombreComercial ?: "")
            binding.etRubro.setText(perfil?.rubro ?: "")
            binding.etFormacion.setText(perfil?.formacion ?: "")
            binding.etExperiencia.setText(perfil?.experiencia ?: "")
            binding.etDescripcion.setText(perfil?.descripcion ?: "")
            binding.etSitioWeb.setText(perfil?.sitioWeb ?: "")
            perfil?.fotoPerfilUrl?.let { binding.ivFoto.load(it) } ?: binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)
            binding.tvCvActual.text = if (perfil?.cvUrl != null) "Documento: disponible" else getString(R.string.perfil_cv)
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

        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()
        val nombreComercial = binding.etNombreComercial.text.toString().trim()
        val rubro = binding.etRubro.text.toString().trim()
        val formacion = binding.etFormacion.text.toString().trim()
        val experiencia = binding.etExperiencia.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val sitioWeb = binding.etSitioWeb.text.toString().trim()

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
                        val cvRef = storage.child("perfiles/${user.uid}/doc_${System.currentTimeMillis()}.pdf")
                        cvRef.putFile(selectedPdfUri!!).await()
                        cvUrlSubido = cvRef.downloadUrl.await().toString()
                    }
                }

                val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
                val snapshot = ref.get().await()

                val updates = mutableMapOf<String, Any?>(
                    "nombre_completo" to (if (nombreCompleto.isBlank()) null else nombreCompleto),
                    "ubicacion" to (if (ubicacion.isBlank()) null else ubicacion),
                    "nombreComercial" to (if (nombreComercial.isBlank()) null else nombreComercial),
                    "rubro" to (if (rubro.isBlank()) null else rubro),
                    "formacion" to (if (formacion.isBlank()) null else formacion),
                    "experiencia" to (if (experiencia.isBlank()) null else experiencia),
                    "descripcion" to (if (descripcion.isBlank()) null else descripcion),
                    "sitioWeb" to (if (sitioWeb.isBlank()) null else sitioWeb)
                )

                if (fotoUrlSubida != null) updates["fotoPerfilUrl"] = fotoUrlSubida
                else if (removePhoto) updates["fotoPerfilUrl"] = null

                if (cvUrlSubido != null) updates["cvUrl"] = cvUrlSubido
                else if (removeCv) updates["cvUrl"] = null

                if (!snapshot.exists()) {
                    val nuevo = EmpleadorProfile(
                        uid = user.uid,
                        nombre_completo = updates["nombre_completo"] as String?,
                        email = user.email,
                        tipoUsuario = "empleador",
                        tiempo_registro = System.currentTimeMillis(),
                        ubicacion = updates["ubicacion"] as String?,
                        nombreComercial = updates["nombreComercial"] as String?,
                        rubro = updates["rubro"] as String?,
                        formacion = updates["formacion"] as String?,
                        experiencia = updates["experiencia"] as String?,
                        descripcion = updates["descripcion"] as String?,
                        sitioWeb = updates["sitioWeb"] as String?,
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
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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