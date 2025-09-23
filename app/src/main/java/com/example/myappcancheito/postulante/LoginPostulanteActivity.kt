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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.myappcancheito.databinding.ActivityLoginPostulanteBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginPostulanteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPostulanteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        credentialManager = CredentialManager.create(this)
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
        binding.cardGoogle.setOnClickListener {
            if (::credentialManager.isInitialized) {
                signInWithGoogle()
            } else {
                Toast.makeText(this, "Error: Credential Manager no inicializado", Toast.LENGTH_SHORT).show()
            }
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

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(com.example.myappcancheito.R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginPostulanteActivity
                )
                handleGoogleCredential(result.credential)
            } catch (e: GetCredentialException) {
                if (e.message?.contains("No credentials available") == true) {
                    Toast.makeText(
                        this@LoginPostulanteActivity,
                        "No hay cuentas de Google disponibles en el dispositivo",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@LoginPostulanteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(this, "Token inválido", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Credencial no es Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val loadingDialog = createLoadingDialog()
        loadingDialog.show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser && user != null) {
                        saveNewGoogleUser(user.uid, user.displayName ?: "", user.email ?: "")
                    } else {
                        verifyUserType(user?.uid ?: "", loadingDialog)
                    }
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Fallo en autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveNewGoogleUser(uid: String, fullName: String, email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
        val timestamp = System.currentTimeMillis()
        val userData = mapOf(
            "uid" to uid,
            "nombre_completo" to fullName,
            "email" to email,
            "tipoUsuario" to "postulante",
            "usuario_verificado" to false,
            "ubicacion"          to null,
            "formacion"          to null,
            "experiencia"        to null,
            "fotoPerfilUrl"      to null,
            "cvUrl"              to null,
            "nombreComercial"    to null,
            "rubro"              to null,
            "descripcion"        to null,
            "estadoCuenta"       to "Activa",
            "tiempo_registro" to timestamp
        )
        dbRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro con Google exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivityPostulante::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
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