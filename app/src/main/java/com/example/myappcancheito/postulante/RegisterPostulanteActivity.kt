package com.example.myappcancheito.postulante

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.ActivityRegisterPostulanteBinding
import com.example.myappcancheito.Constantes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterPostulanteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterPostulanteBinding
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterPostulanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Ir al login de postulante
        binding.tvIrLoginPos.setOnClickListener {
            startActivity(Intent(this, LoginPostulanteActivity::class.java))
            finish()
        }

        binding.btnRegistrarmePos.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val fullName = binding.etNombreCompletoPos.text.toString().trim()
        val email = binding.etCorreoPos.text.toString().trim()
        val password = binding.etContrasenaPos.text.toString().trim()
        val confirmPassword = binding.etConfirmarContrasenaPos.text.toString().trim()
        when {
            fullName.isBlank() -> binding.etNombreCompletoPos.error = "Ingrese su nombre completo"
            email.isBlank() -> binding.etCorreoPos.error = "Ingrese su correo"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.etCorreoPos.error = "Correo no válido"
            password.isBlank() -> binding.etContrasenaPos.error = "Ingrese una contraseña"
            password.length < 6 -> binding.etContrasenaPos.error = "Contraseña debe tener al menos 6 caracteres"
            confirmPassword.isBlank() -> binding.etConfirmarContrasenaPos.error = "Confirme la contraseña"
            password != confirmPassword -> binding.etConfirmarContrasenaPos.error = "Las contraseñas no coinciden"
            else -> CoroutineScope(Dispatchers.IO).launch {
                registerPostulante(fullName, email, password)
            }
        }
    }

    private suspend fun registerPostulante(fullName: String, email: String, password: String) {
        try {
            withContext(Dispatchers.Main) { showToast("Creando cuenta...") }
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            saveUserData(authResult.user!!.uid, fullName, email)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { showToast("Error en el registro: ${e.message}") }
        }
    }

    private suspend fun saveUserData(uid: String, fullName: String, email: String) {
        try {
            withContext(Dispatchers.Main) { showToast("Guardando información...") }
            val timestamp = Constantes().obtenerTiempoD()
            val userData = mapOf(
                "uid" to uid,
                "nombre_completo" to fullName,
                "email" to email,
                "tipoUsuario" to "postulante",
                "tiempo_registro" to timestamp
            )
            database.getReference("Usuarios").child(uid).setValue(userData).await()
            withContext(Dispatchers.Main) {
                showToast("Registro exitoso")
                startActivity(Intent(this@RegisterPostulanteActivity, MainActivityPostulante::class.java))
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { showToast("Error al guardar: ${e.message}") }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}