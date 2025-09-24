package com.example.myappcancheito.empleador

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.ActivityRegisterEmpleadorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.myappcancheito.Constantes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterEmpleadorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterEmpleadorBinding
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterEmpleadorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginEmpleadorActivity::class.java))
            finish()
        }
        binding.btnRegistrarme.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val fullName = binding.etNombreCompleto.text.toString().trim()
        val email = binding.etCorreo.text.toString().trim()
        val password = binding.etContrasena.text.toString().trim()
        val confirmPassword = binding.etConfirmarContrasena.text.toString().trim()

        when {
            fullName.isBlank() -> binding.etNombreCompleto.error = "Ingrese su nombre completo"
            email.isBlank() -> binding.etCorreo.error = "Ingrese su correo"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.etCorreo.error = "Correo no válido"
            password.isBlank() -> binding.etContrasena.error = "Ingrese una contraseña"
            password.length < 6 -> binding.etContrasena.error = "Contraseña debe tener al menos 6 caracteres"
            confirmPassword.isBlank() -> binding.etConfirmarContrasena.error = "Confirme la contraseña"
            password != confirmPassword -> binding.etConfirmarContrasena.error = "Las contraseñas no coinciden"
            else -> CoroutineScope(Dispatchers.IO).launch {
                registerEmployer(fullName, email, password)
            }
        }
    }

    private suspend fun registerEmployer(fullName: String, email: String, password: String) {
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
                "tipoUsuario" to "empleador",
                "usuario_verificado" to false,
                "tiempo_registro" to timestamp
            )
            database.getReference("Usuarios").child(uid).setValue(userData).await()
            withContext(Dispatchers.Main) {
                showToast("Registro exitoso")
                startActivity(Intent(this@RegisterEmpleadorActivity, EmpleadorActivity::class.java))
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