package com.example.myappcancheito.postulante

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myappcancheito.databinding.ActivityLoginPostulanteBinding
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
class LoginPostulanteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPostulanteBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginPostulanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLoginPos.setOnClickListener {
            validarInfo()
        }

        binding.tvIrRegistroPos.setOnClickListener {
            startActivity(Intent(this, RegisterPostulanteActivity::class.java))
        }
    }

    private fun validarInfo() {
        email = binding.etCorreoPos.text.toString().trim()
        password = binding.etContrasenaPos.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.etCorreoPos.error = "Ingrese email"
                binding.etCorreoPos.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etCorreoPos.error = "Email no válido"
                binding.etCorreoPos.requestFocus()
            }
            password.isEmpty() -> {
                binding.etContrasenaPos.error = "Ingrese contraseña"
                binding.etContrasenaPos.requestFocus()
            }
            else -> {
                loginPostulante()
            }
        }
    }

    private fun loginPostulante() {
        val loadingDialog = AlertDialog.Builder(this)
            .setTitle("Espere por favor")
            .setMessage("Ingresando...")
            .setCancelable(false)
            .create()
        loadingDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                database.getReference("Usuarios").child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        loadingDialog.dismiss()
                        val tipoUsuario = snapshot.child("tipoUsuario").value?.toString()

                        if (tipoUsuario == "postulante") {
                            Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivityPostulante::class.java))
                            finishAffinity()
                        } else {
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this,
                                "Este usuario no es postulante, ingrese en la opción correcta",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Error al verificar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(this, "No se pudo iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}