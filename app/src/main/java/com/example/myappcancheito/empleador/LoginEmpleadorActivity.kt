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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

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

        binding.tvOlvideContrasena.setOnClickListener {
            showResetPasswordDialog()
        }
    }

    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_email_post, null)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_email)
        val btnSend = dialogView.findViewById<MaterialButton>(R.id.btn_send)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            when {
                email.isEmpty() -> etEmail.error = "Ingrese un correo"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> etEmail.error = "Correo no válido"
                else -> {
                    sendPasswordResetEmail(email, dialog)
                }
            }
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sendPasswordResetEmail(email: String, dialog: AlertDialog) {
        if (!isOnline()) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_LONG).show()
            return
        }
        val loadingDialog = createLoadingDialog()
        loadingDialog.show()
        firebaseAuth.setLanguageCode("es")
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(this, "Enlace de restablecimiento enviado a $email", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No se encontró una cuenta con este correo"
                        is FirebaseAuthInvalidCredentialsException -> "Correo con formato inválido"
                        else -> task.exception?.message ?: "Error desconocido"
                    }
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createLoadingDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle("Espere por favor")
            .setMessage("Ingresando...")
            .setCancelable(false)
            .create()
    }
    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
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