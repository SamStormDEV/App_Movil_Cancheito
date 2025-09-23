package com.example.myappcancheito.empleador

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myappcancheito.R
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import com.example.myappcancheito.databinding.ActivityLoginEmprendedorBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog

class LoginEmpleadorActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginEmprendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginEmprendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            validarInfo()
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterEmpleadorActivity::class.java))
        }
    }

    private fun validarInfo() {
        email = binding.etCorreo.text.toString().trim()
        password = binding.etContrasena.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.etCorreo.error = "Ingrese email"
                binding.etCorreo.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etCorreo.error = "Email no válido"
                binding.etCorreo.requestFocus()
            }
            password.isEmpty() -> {
                binding.etContrasena.error = "Ingrese contraseña"
                binding.etContrasena.requestFocus()
            }
            else -> {
                loginEmpleador()
            }
        }
    }

    private fun loginEmpleador() {
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
                        val estadoCuenta = snapshot.child("estadoCuenta").value?.toString()

                        if (tipoUsuario == "empleador") {
                            if (estadoCuenta == "Activa") {
                                Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, EmpleadorActivity::class.java))
                                finishAffinity()
                            } else {
                                firebaseAuth.signOut()
                                Toast.makeText(this, "Cuenta suspendida", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this,
                                "Este usuario no es empleador, ingrese en la opción correcta",
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