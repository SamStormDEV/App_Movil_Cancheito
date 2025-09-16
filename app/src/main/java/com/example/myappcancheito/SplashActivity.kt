package com.example.myappcancheito

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myappcancheito.empleador.EmpleadorActivity
import com.example.myappcancheito.postulante.MainActivityPostulante
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val saludoTextView = findViewById<TextView>(R.id.txtSaludo)
        saludoTextView.text = getGreeting()
        saludoTextView.alpha = 0f
        saludoTextView.translationY = 100f
        saludoTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1500)
            .start()

        iniciarCuentaRegresiva()

        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "¡Buenos días! 😉"
            in 12..18 -> "¡Buenas tardes! 😎"
            else -> "¡Buenas noches! 🤓"
        }
    }

    private fun iniciarCuentaRegresiva() {
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                comprobarTipoUsuario()
            }
        }.start()
    }

    private fun comprobarTipoUsuario() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, SelecionarTipoActivity::class.java))
        }else{
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("tipoUsuario").value
                        if (userType == "empleador") {
                            startActivity(Intent(this@SplashActivity, EmpleadorActivity::class.java))
                            finish()
                        } else if (userType == "postulante") {
                            startActivity(Intent(this@SplashActivity, MainActivityPostulante::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }
}
