package com.example.myappcancheito

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val saludoTextView = findViewById<TextView>(R.id.txtSaludo)
        saludoTextView.text = getGreeting()

        // Animación moderna en el saludo
        saludoTextView.alpha = 0f
        saludoTextView.translationY = 100f
        saludoTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1500)
            .start()

        iniciarCuentaRegresiva()
    }

    // Función para saludo dinámico según la hora
    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "¡Buenos días!"
            in 12..18 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }

    // Timer de 4 segundos para ir a MainActivity
    private fun iniciarCuentaRegresiva() {
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }.start()
    }
}
