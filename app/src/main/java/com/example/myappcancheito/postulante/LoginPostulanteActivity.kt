package com.example.myappcancheito.postulante

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myappcancheito.databinding.ActivityLoginPostulanteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginPostulanteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPostulanteBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
        }
        binding = ActivityLoginPostulanteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        FirebaseDatabase.getInstance().getReference("Usuarios").keepSynced(true)
        binding.btnLoginPos.setOnClickListener {
            validarInfo()
        }
        binding.tvIrRegistroPos.setOnClickListener {
            startActivity(Intent(this, RegisterPostulanteActivity::class.java))
        }
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            verifyUserType(currentUser.uid)
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

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun loginPostulante() {
        val loadingDialog = createLoadingDialog()
        loadingDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                verifyUserType(uid, loadingDialog)
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                if (!isOnline()) {
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null && currentUser.uid.isNotEmpty()) {
                        verifyUserType(currentUser.uid, loadingDialog)
                    } else {
                        Toast.makeText(
                            this,
                            "No se pudo iniciar sesión sin conexión. Verifica tus credenciales.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "No se pudo iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verifyUserType(uid: String, loadingDialog: AlertDialog? = createLoadingDialog()) {
        loadingDialog?.show()
        val dbRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
        dbRef.get()
            .addOnSuccessListener { snapshot ->
                loadingDialog?.dismiss()
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
                loadingDialog?.dismiss()
                if (!isOnline()) {
                    dbRef.get().addOnSuccessListener { snapshot ->
                        val tipoUsuario = snapshot.child("tipoUsuario").value?.toString()
                        if (tipoUsuario == "postulante") {
                            Toast.makeText(this, "Bienvenido(a) (Offline)", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivityPostulante::class.java))
                            finishAffinity()
                        } else {
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this,
                                "Usuario no es postulante (Offline).",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener {
                        firebaseAuth.signOut()
                        Toast.makeText(
                            this,
                            "Error al verificar usuario sin conexión.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    firebaseAuth.signOut()
                    Toast.makeText(this, "Error al verificar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
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
}