package com.example.myappcancheito.empleador

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myappcancheito.R
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Toast


class EmpleadorActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()
        setContentView(R.layout.activity_empleador)
    }

    private fun comprobarSesion() {
        if (firebaseAuth!!.currentUser == null) {
            startActivity(Intent(applicationContext, Register_empleador_Activity::class.java))
            Toast.makeText(applicationContext, "Registrate", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(applicationContext, "Bienvenido", Toast.LENGTH_SHORT).show()
        }
    }
}