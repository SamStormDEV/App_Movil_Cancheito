package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import coil.load
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentPerfilPostulanteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch

class FragmentPerfilP : Fragment(R.layout.fragment_perfil_postulante) {

    private var _binding: FragmentPerfilPostulanteBinding? = null
    private val binding get() = _binding!!

    // Selecciones locales
    private var selectedImageUri: Uri? = null
    private var selectedPdfUri: Uri? = null

    // Flags para eliminar en DB si el usuario pulsa "Quitar"
    private var removePhoto: Boolean = false
    private var removeCv: Boolean = false

    // Launchers para elegir archivos
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            removePhoto = false
            binding.ivFoto.load(uri)
        }
    }

    private val pickPdfLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
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
            binding.tvCvActual.text = "CV: (ninguno)"
        }

        binding.btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun cargarPerfil() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Inicia sesión para ver tu perfil", Toast.LENGTH_LONG).show()
            binding.progress.visibility = View.GONE
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(user.uid)

        ref.get().addOnSuccessListener { snapshot ->
            binding.progress.visibility = View.GONE
            if (!snapshot.exists()) {
                // Mostrar datos básicos desde Auth (solo lectura)
                binding.tvNombre.text = "Nombre: ${user.displayName ?: "-"}"
                binding.tvEmail.text  = "Correo: ${user.email ?: "-"}"
                binding.tvTipo.text   = "Tipo: -"
                binding.tvRegistro.text = "Registrado: -"
                // Reset flags
                removePhoto = false
                removeCv = false
                return@addOnSuccessListener
            }

            val perfil = snapshot.getValue(PostulanteProfile::class.java)
            val nombre = perfil?.nombre_completo ?: user.displayName ?: "-"
            val email = perfil?.email ?: user.email ?: "-"
            val tipo = perfil?.tipoUsuario ?: "-"
            val registrado = formatearTiempo(perfil?.tiempo_registro)

            // SOLO LECTURA
            binding.tvNombre.text = "Nombre: $nombre"
            binding.tvEmail.text  = "Correo: $email"
            binding.tvTipo.text   = "Tipo: $tipo"
            binding.tvRegistro.text = "Registrado: $registrado"

            // Editables
            binding.etUbicacion.setText(perfil?.ubicacion ?: "")
            binding.etFormacion.setText(perfil?.formacion ?: "")
            binding.etExperiencia.setText(perfil?.experiencia ?: "")

            val fotoUrl = perfil?.fotoPerfilUrl
            if (!fotoUrl.isNullOrBlank()) binding.ivFoto.load(fotoUrl)
            else binding.ivFoto.setImageResource(R.mipmap.ic_launcher_round)

            val cvUrl = perfil?.cvUrl
            binding.tvCvActual.text = if (!cvUrl.isNullOrBlank())
                "CV: disponible"
            else
                "CV: (ninguno)"


            removePhoto = false
            removeCv = false

        }.addOnFailureListener { e ->
            binding.progress.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambios() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Inicia sesión para guardar", Toast.LENGTH_LONG).show()
            return
        }

        // Solo campos editables
        val ubicacion = binding.etUbicacion.text.toString().trim()
        val formacion = binding.etFormacion.text.toString().trim()
        val experiencia = binding.etExperiencia.text.toString().trim()

        binding.progress.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1) Subir archivos si corresponde
                val storage = FirebaseStorage.getInstance().reference
                var fotoUrlSubida: String? = null
                var cvUrlSubido: String? = null

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

                // 2) Guardar en RTDB (NO tocar nombre ni correo)
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
                val snapshot = ref.get().await()

                val updates = mutableMapOf<String, Any?>(
                    "ubicacion" to ubicacion.ifBlank { null },
                    "formacion" to formacion.ifBlank { null },
                    "experiencia" to experiencia.ifBlank { null }
                )

                // Foto
                when {
                    fotoUrlSubida != null -> updates["fotoPerfilUrl"] = fotoUrlSubida
                    removePhoto -> updates["fotoPerfilUrl"] = null   // borrar clave
                }

                // CV
                when {
                    cvUrlSubido != null -> updates["cvUrl"] = cvUrlSubido
                    removeCv -> updates["cvUrl"] = null             // borrar clave
                }

                if (!snapshot.exists()) {
                    // Crear perfil nuevo con nombre/correo de Auth (solo lectura) y demás editables
                    val nuevo = PostulanteProfile(
                        uid = user.uid,
                        nombre_completo = user.displayName,
                        email = user.email,
                        tipoUsuario = "Postulante",
                        tiempo_registro = System.currentTimeMillis(),
                        ubicacion = updates["ubicacion"] as String?,
                        formacion = updates["formacion"] as String?,
                        experiencia = updates["experiencia"] as String?,
                        fotoPerfilUrl = updates["fotoPerfilUrl"] as String?,
                        cvUrl = updates["cvUrl"] as String?
                    )
                    ref.setValue(nuevo).await()
                } else {
                    // No toques nombre_completo/email: no se incluyen en updates
                    ref.updateChildren(updates as Map<String, Any?>).await()
                }

                withContext(Dispatchers.Main) {
                    binding.progress.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()

                    // Reset
                    selectedImageUri = null
                    selectedPdfUri = null
                    removePhoto = false
                    removeCv = false

                    cargarPerfil()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progress.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** (Solo se usa si algún día requieres reauth para otros cambios) */
    private suspend fun pedirPasswordYReautenticar(emailActual: String): Boolean =
        withContext(Dispatchers.Main) {
            var result = false
            val editText = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                hint = "Contraseña actual"
                setPadding(32, 24, 32, 24)
            }
            val latch = CountDownLatch(1)

            AlertDialog.Builder(requireContext())
                .setTitle("Reautenticación requerida")
                .setMessage("Para continuar, ingresa tu contraseña de $emailActual")
                .setView(editText)
                .setPositiveButton("Confirmar") { _, _ -> latch.countDown() }
                .setNegativeButton("Cancelar") { _, _ -> latch.countDown() }
                .setCancelable(false)
                .show()

            withContext(Dispatchers.IO) { latch.await() }
            result
        }

    private fun formatearTiempo(valor: Any?): String {
        return when (valor) {
            is Number -> {
                val millis = valor.toLong()
                val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                df.format(Date(millis))
            }
            is String -> valor
            else -> "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
