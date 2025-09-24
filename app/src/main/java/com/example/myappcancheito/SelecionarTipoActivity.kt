package com.example.myappcancheito

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myappcancheito.databinding.ActivitySelecionarTipoBinding
import com.example.myappcancheito.empleador.LoginEmpleadorActivity
import com.example.myappcancheito.postulante.LoginPostulanteActivity

class SelecionarTipoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelecionarTipoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelecionarTipoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tipoempleador.setOnClickListener {
            startActivity(Intent(this, LoginEmpleadorActivity::class.java))
        }
        binding.tipopostulante.setOnClickListener {
            startActivity(Intent(this, LoginPostulanteActivity::class.java))
        }
    }
}